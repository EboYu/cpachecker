/* Generated by CIL v. 1.3.7 */
/* print_CIL_Input is true */

#line 1 "/home/merry/Desktop/function_call.c"
int compute_square(int y ) 
{ 

  {
#line 3
  return (y + 3);
}
}
#line 6 "/home/merry/Desktop/function_call.c"
int main(void) 
{ int x ;
  int tmp ;
  int tmp___0 ;

  {
  {
#line 8
  x = 2;
#line 9
  tmp = compute_square(x);
#line 9
  x = tmp + 2;
#line 10
  tmp___0 = compute_square(x);
#line 10
  x = tmp___0 + 2;
  }
#line 11
  if (x != 12) {
    goto ERROR;
  } else {

  }
#line 15
  return (0);
  ERROR: 
#line 17
  return (-1);
}
}
