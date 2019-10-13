import java.io.*;   //this imports the input and output libraries
import java.net.*;  //this imports the libraries for networking
import java.util.*;



public class Test{

    public static void main(String args[]){
        String re = "/cgi/addnums.fake-cgi?person=YourName&num1=4&num2=34";
        String name = re.substring(re.indexOf("=")+1,re.indexOf("&"));
        String num1 = re.substring(re.indexOf("&num1")+6, re.indexOf("&num2"));
        String num2 = re.substring(re.indexOf("&num2")+6);
        int iNum1 = Integer.parseInt(num1);
        int iNum2 = Integer.parseInt(num2);
        System.out.println(name+" Result: "+(iNum1+iNum2));
        System.out.println(num1);
        System.out.println(num2);
    }
}