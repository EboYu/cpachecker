if [ "$1" = "-csurf" ];then
    echo "Build CFA of the project"
    exec ./scripts/runJavaPlugin.sh $*
elif [ "$1" = "-csurfcheck" ];then
    echo "Build CFA and Perform model checking the project"
    exec ./scripts/runJavaPlugin.sh $*
elif [ "$1" = "-load" ];then
    echo "Load CFA and Perform model checking on the project"
    exec ./scripts/cpa.sh $*
else
    echo "Not support this mode: $1"
    shift;
    exit 1
fi

exit 1