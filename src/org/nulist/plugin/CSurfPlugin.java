package org.nulist.plugin;

import com.google.common.io.MoreFiles;
import com.grammatech.cs.*;
import org.nulist.plugin.model.action.ITTIAbstract;
import org.nulist.plugin.parser.CFABuilder;
import org.nulist.plugin.parser.CFGFunctionBuilder;
import org.nulist.plugin.parser.CFGParser;
import org.nulist.plugin.parser.FuzzyParser;
import org.sosy_lab.cpachecker.cfa.ast.ADeclaration;
import org.sosy_lab.cpachecker.cfa.ast.c.CBinaryExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CFunctionDeclaration;
import org.sosy_lab.cpachecker.cfa.ast.c.CIntegerLiteralExpression;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.c.CAssumeEdge;
import org.sosy_lab.cpachecker.cfa.types.MachineModel;
import org.sosy_lab.cpachecker.cfa.types.c.CType;
import org.sosy_lab.cpachecker.cmdline.CPAMain;

import java.io.*;
import java.lang.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.nulist.plugin.model.ChannelBuildOperation.doComposition;
import static org.nulist.plugin.parser.CFGParser.*;
import static org.nulist.plugin.parser.FuzzyParser.channel;
import static org.nulist.plugin.util.CFGDumping.dumpCFG2Dot;
import static org.nulist.plugin.util.ClassTool.*;
//Combine CPAChecker as a plugin of CodeSurfer

public class CSurfPlugin {
    private final static String ENBProjectPath = "/OAI-ENB/OAI-ENB.prj_files/OAI-ENB.sdg";
    private final static String MMEProjectPath = "/OAI-MME/OAI-MME.prj_files/OAI-MME.sdg";
    private final static String UEProjectPath = "/OAI-UE/OAI-UE.prj_files/OAI-UE.sdg";



    /**
     * @Description
     * @Param [args]
     * @return void
     **/
    public static void main(String args[]){

        //Arguments to CPAChecker
        String[] arguments = null;
        String cpacheckPath ="";
        String projectPath = "";
        if(args.length>=3){
            cpacheckPath = args[0];
            projectPath = args[1];
            arguments = args[2].split(" ");
        }

        //read serialized cfa and perform model checking
//        readSerializedCFA(arguments,cpacheckPath, projectPath);
        //read cfg files and translate them into cfa
        readCFGFiles(arguments,cpacheckPath,projectPath);

    }

    public static void readSerializedCFA(String[] arguments, String cpacheckPath,String projectPath){
        String projPath = System.getProperty("user.dir");
        CPAMain cpaMain = new CPAMain(arguments, cpacheckPath);
        String cfafile = projPath+"/output/cfa.ser.gz";
        File file = new File(cfafile);
        if(file.exists())
            CPAMain.executeParser(arguments,cpacheckPath, projectPath, null);
        else
            printWARNING("There is no cfa.ser.gz in "+cfafile);
    }

    public static void readCFGFiles(String[] arguments, String cpacheckPath, String projectPath){
        try{
            String projPath = System.getProperty("user.dir");
            CPAMain cpaMain = new CPAMain(arguments, cpacheckPath);
            CFGParser cfgParser = new CFGParser(cpaMain.logManager, MachineModel.LINUX64);
            Map<String, CFABuilder> builderMap = new HashMap<>();

            printINFO("==================CSURF_PLUGIN_BEGIN==================");

            printINFO("==================Parsing UE==================");
            project.load(projectPath+UEProjectPath,true);
            project proj = project.current();
//            CPAMain.executeParser(arguments, cpacheckPath, projectPath+UEProjectPath, proj);
//            CPAMain.executionTesting(arguments, cpacheckPath, projectPath+UEProjectPath, proj);
            try {
                CFABuilder cfaBuilder = cfgParser.parseBuildProject(proj);
                builderMap.put(proj.name(),cfaBuilder);
            }catch (result r){
                r.printStackTrace();
            }
            printINFO("==================Finish UE==================");

            printINFO("==================Parsing ENB==================");
            project.load(projectPath+ENBProjectPath,true);
            proj = project.current();
//            CPAMain.executionTesting(arguments, cpacheckPath, projectPath+ENBProjectPath, proj);
            try {
                CFABuilder cfaBuilder = cfgParser.parseBuildProject(proj);
                builderMap.put(proj.name(),cfaBuilder);
            }catch (result r){
                r.printStackTrace();
            }
//
//            //CPAMain.executionTesting(arguments, cpacheckPath, projectPath+MMEProjectPath, proj);
//
            printINFO("==================Finish ENB==================");

            printINFO("==================Parsing MME==================");
            project.load(projectPath+MMEProjectPath,true);
            proj = project.current();
//            CPAMain.executionTesting(arguments, cpacheckPath, projectPath+MMEProjectPath, proj);
            try {
                CFABuilder cfaBuilder = cfgParser.parseBuildProject(proj);
                builderMap.put(proj.name(),cfaBuilder);
            }catch (result r){
                r.printStackTrace();
            }
            printINFO("==================Finish MME==================");
            project.unload();

            compareGlobalName(builderMap);
            printINFO("==================Parsing Message Channel Model==================");
            FuzzyParser fuzzyParser = new FuzzyParser(cpaMain.logManager, MachineModel.LINUX64, builderMap);

            String channelModelFile =projPath+"/libmodels/channel";
            fuzzyParser.parseChannelModel(channelModelFile);
            builderMap.put(channel,fuzzyParser.getChannelBuilder());
            if(builderMap.size()>1)
                doComposition(builderMap);

            for(String key: builderMap.keySet()){
                CFABuilder cfgBuilder = builderMap.get(key);
                for(CFGFunctionBuilder functionBuilder :cfgBuilder.cfgFunctionBuilderMap.values()){
                    if(!functionBuilder.isFinished)
                        functionBuilder.emptyFunction();
                    //printf("The function :"+functionBuilder.functionName+" is not finished in "+key);
                }
            }
            printINFO("==================Finish Message Channel Model==================");

            cpaMain.CFACombination(builderMap);

            printINFO("==================CSURF_PLUGIN_END==================");
        }catch(result r){
            System.out.println("Uncaught exception: " + r);
        }
    }

    private static void compareGlobalName(Map<String, CFABuilder> builderMap){
        try {
            FileWriter fileWriter = new FileWriter(new File("nameConflict.txt"));
            Map<Integer, ADeclaration> ueGlobalDeclarations = builderMap.get(UE).expressionHandler.globalDeclarations;
            Map<Integer, ADeclaration> enbGlobalDeclarations = builderMap.get(ENB).expressionHandler.globalDeclarations;
            Map<Integer, ADeclaration> mmeGlobalDeclarations = builderMap.get(MME).expressionHandler.globalDeclarations;
            if(ueGlobalDeclarations.containsKey("UE_malloc".hashCode()))
                printf("There is UE_malloc in UE");
            if(enbGlobalDeclarations.containsKey("ENB_malloc".hashCode()))
                printf("There is ENB_malloc in ENB");
            if(mmeGlobalDeclarations.containsKey("MME_malloc".hashCode()))
                printf("There is MME_malloc in MME");
//            for(Integer i:ueGlobalDeclarations.keySet()){
//                if(enbGlobalDeclarations.containsKey(i)){
//                    ADeclaration ueDeclaration = ueGlobalDeclarations.get(i);
//                    ADeclaration enbDeclaration = enbGlobalDeclarations.get(i);
//                    boolean isFunction1 = ueDeclaration instanceof CFunctionDeclaration;
//                    boolean isFunction2 = enbDeclaration instanceof CFunctionDeclaration;
//                    fileWriter.write(ueDeclaration.getName()+" is "+isFunction1+ " in UE has the same name with "+ enbDeclaration.getName()+" is "+isFunction2+" in ENB\n");
//                }
//
//                if(mmeGlobalDeclarations.containsKey(i)){
//                    ADeclaration ueDeclaration = ueGlobalDeclarations.get(i);
//                    ADeclaration mmeDeclaration = mmeGlobalDeclarations.get(i);
//                    boolean isFunction1 = ueDeclaration instanceof CFunctionDeclaration;
//                    boolean isFunction2 = mmeDeclaration instanceof CFunctionDeclaration;
//                    fileWriter.write(ueDeclaration.getName()+" is "+isFunction1+ " in UE has the same name with "+ mmeDeclaration.getName()+" is "+isFunction2+ " in MME\n");
//                }
//
//            }
//            fileWriter.flush();
//            for(Integer i:enbGlobalDeclarations.keySet()){
//
//
//                if(mmeGlobalDeclarations.containsKey(i)){
//                    ADeclaration enbDeclaration = enbGlobalDeclarations.get(i);
//                    ADeclaration mmeDeclaration = mmeGlobalDeclarations.get(i);
//                    boolean isFunction1 = enbDeclaration instanceof CFunctionDeclaration;
//                    boolean isFunction2 = mmeDeclaration instanceof CFunctionDeclaration;
//                    fileWriter.write(enbDeclaration.getName()+" is "+isFunction1+ " in ENB has the same name with "+ mmeDeclaration.getName()+" is "+isFunction2+ " in MME\n");
//                }
//            }
            fileWriter.flush();
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private static void serializeCFABuilder(Map<String, CFABuilder> builderMap){
        builderMap.forEach((projectname, builder)->{
            Path serializeCfaFile = Paths.get(projectname+".ser.gz");
            try {
                MoreFiles.createParentDirectories(serializeCfaFile);
                try (OutputStream outputStream = Files.newOutputStream(serializeCfaFile);
                     OutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
                     ObjectOutputStream oos = new ObjectOutputStream(gzipOutputStream)) {
                    oos.writeObject(builder);
                }
            } catch (IOException e) {
                e.printStackTrace();
                printWARNING("Could not serialize CFA to file.");
            }
        });


    }


    private static void dumpCFG(project target, String path) throws result{

        for( project_compunits_iterator cu_it = target.compunits();
             !cu_it.at_end();
             cu_it.advance() )
        {
            compunit cu = cu_it.current();//each shall be a C file

            if(!cu.is_user())
                continue;
            // Iterate over all procedures in the compilation unit
            // procedure = function
            for( compunit_procedure_iterator proc_it = cu.procedures();
                 !proc_it.at_end();
                 proc_it.advance() )
            {
                procedure proc = proc_it.current();

                if(proc.get_kind().equals(procedure_kind.getUSER_DEFINED())){
                    dumpCFG2Dot(proc, path);
                }
            }
        }
    }

}
