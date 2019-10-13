/*--------------------------------------------
1. Name / Date: 
Werner Reineke-Ryskiewicz
12.10.2019

2. Java Version Used: 1.8.0_211-b12

3. you may compile this code as follows: 
$javac MyWebServer.java
or
$javac *.java
This will be usefull when using multiple files

4.If you wish to run this particular piece of hardware open a terminal and after comilation type:
$java MyWebServer

5. This is the only file you require to run the Server. You can traverse the filestructure, 
open files view their content and return to the "home" folder. 

6. Notes:

This is the MyWebServer

I had no idea that there was so little code needed to get something like this done.
It's almost like an ftp server.

Version 13.10.19

-----------------------------------------------*/
/**
 * These are all of the important java libraries that are used in the process of this Server.
 */
import java.io.*;   //this imports the input and output libraries
import java.net.*;  //this imports the libraries for networking
import java.util.*; //this utility is useful for File utilization (printing to a file and returning filenames and paths)

/**
 * The BrowseWorker extends the Thread and is used to spin off multiple threads
 * and satisfy clients browsing the contents of the folder structure. It also displays
 * files and can recognize the addnums.fake-cgi--file which takes the forms name and
 * adds the two numbers in the field.
 * 
 * Thanks to Clark Elliot for the addnums.html file, the tips on html websites
 * and all the other valuable information on his homework assignment website.
 * 
 * Also many thanks to Kishori, dhanyu and Clark Elliot's reference to them for the 
 * File reading.
 * 
 */
class BrowseWorker extends Thread{    
    
    Socket sock; 

    BrowseWorker (Socket s) { 
        sock = s; 
    }

    /**
     * The addnums function is designed to interpret the addnums.html file
     * and the return information sent by the Firefox Browser. Essentially, it parses the
     * link sent back and finds the name and two integers entered into the field.
     * This information is written to an html file and sent back to the Browser
     * containing the processed information:
     * - answering the user personally 
     * - adding the two numbers the user entered.
     * (The file is created to make it easier to count the byte length of the file sent
     * to the server. It is also deleted afterwards.)
     * 
     * IMPORTANT: addnums() can only calculate with integers.
     * 
     * @param request is the parsed information sent by the Browser ready for interpretation
     * by the Server.
     * @param out out is the Print stream to send the information back to the Browser.
     */
    private static void addnums(String request, PrintStream out){

        try{
            //here I use a File and a BufferedWriter to write to a file 
            //to make sending it to a Browser simpeler
            File toSend = new File("send_site_MyWebServer.html");
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(toSend));

            String name = request.substring(request.indexOf("="));
            
            /**
             * this is where the parsing of name and numbers happens:
             * the name follows the = sign and ends at the first & (found out when using MyListener)
             * the numbers have tags so I used the beginnin index of those tags plus the length
             * to distiguish the beginning and end of them. Furthermore since I am working
             * with integers I used the Integer method to cast to an int. Adding them is the 
             * simpelest step.
             */
            String userName = request.substring(request.indexOf("=")+1,request.indexOf("&"));
            userName = userName.replace("+", " ");
            String num1 = request.substring(request.indexOf("&num1")+6, request.indexOf("&num2"));
            String num2 = request.substring(request.indexOf("&num2")+6);
            int iNum1 = Integer.parseInt(num1);
            int iNum2 = Integer.parseInt(num2);

            // dynmaic File design.
            fileWriter.write("<h1>[W] AddNums Function</h1><pre>\n");
            fileWriter.write("<hr>\n");
            // This is the actual answer to the CGI
            fileWriter.write("<h3>Dear "+userName+" adding "+ num1 + " and "+num2+" equals "+ (iNum1+iNum2)+"<h3>");
            
            fileWriter.write("</pre>\n");
            fileWriter.write("</html>\n");
            fileWriter.close(); // close Filewriter

            /**
             * Here the file becomes useful and I can use it to calculate the file size.
             * I also use my Helper functions that are described at another place to 
             * send a dynamic header and the dynamically created html file to the 
             * Browser that requested it.
             */
            long fileSize = toSend.length(); 
            sendHeader(out, "html", fileSize); 
            sendFileContent("/send_site_MyWebServer.html", out);
            toSend.delete();
        }
        catch(IOException x){
            x.printStackTrace();
        }
    }

    /**
     * The function sendFileContent is a helper function that sends a File
     * to the Browser. This is only the file content that is sent to the Browser
     * the header information is send with another helper function (sendHeader()).
     * This function uses a buffer reads the file that is meant for the Browser
     * (while also catching exceptions) and sends its content line by line.
     * when finished it closes the reader.
     * 
     * @param request is the location of the information that is meant to be sent
     * @param out is the PrintStream that handels information transfer to the Browser
     */
    private static void sendFileContent(String request, PrintStream out){
        
        try{
            BufferedReader reader = new BufferedReader(new FileReader(request.substring(1)));
            String content = reader.readLine();
            while(content != null){
                out.println(content);
                content = reader.readLine();
            }
            reader.close();
        }        
        catch(IOException x){
            x.printStackTrace();
        }
    }
    
    /**
     * The sendHeader function is a simple function that recieves a PrintStream 
     * for information transfer to the Browser, a String type which is the type
     * of file that follows this header and a long that is the amount of data 
     * that is to be expected.
     * 
     * @param out PrintStream to Send Data
     * @param type this is the type of file that will be transfered: either
     * text/plain or text/html
     * @param l is the amount of bytes that will be sent to  the Browser
     */
    private static void sendHeader(PrintStream out, String type, long l){
        out.print("HTTP/1.1 200 OK"+"Server: Fox Server"
        +"Content-Length:"+l+"Connection: close"+"Content-Type: text/"+type+"\r\n\r\n");
    }

    /**
     * This is a simple yet important function. It restricts the program
     * to the directory in which the Server functions and strips the directories
     * for directory traversal.
     * In short, when accessing the parent directory this is what processess the actual
     * location one is searching for. Until it reaches the location at which the server is
     * located at which it stops-
     * @param dir is the current directory that is to be stripped (like the police would do it)
     * @return returns the parent directory unless already at the root of the Server
     */
    private static String parentDirectory(String dir){
        String result = "/";
        if(dir == result || dir.lastIndexOf("/")==0){ // just because it makes this look cooler
            return result;
        }
        return dir.substring(0,dir.lastIndexOf("/")); // this is where I strip the subdirectory
    }


    /**
     * The function displayWebpage recieves the requested directory for traversal and
     * a PrintStream to which it sends the information (this is the Browser).
     * It creates a file send_site_MyWebServer.html and writes the website to this file
     * depending on the contents of the requested folder. It prints all files and
     * folders to the html website as requested by Prof Elliott and creates
     * links that can be followed to access the subdirectories and parent directories
     * respectively. It also ignores itself when traversing the filestructure
     * (send_site_myWebServer.html will be ignored and not display) and the file will
     * be deleted at the end of the function.
     * @param request is the requested location
     * @param out is the Printstream to which to send the information
     */
    private static void displayWebpage(String request, PrintStream out){
        try
        {    
            /**
             * creating the file and the BufferedWriter to write to the file
             */
            File toSend = new File("send_site_MyWebServer.html");
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(toSend));

            fileWriter.write("<h1>[W] Serving Files</h1><pre>\n");
            fileWriter.write("TYPE  <a href='?C=N;O=D'>Name</a>                    <a href='?C=S;O=A'>Size</a>\n");
            fileWriter.write("<hr>\n");
            File f = new File("."+request);
            
            /**
             * this was supposed to omit parent Directory from the list when at Server root
             * However, it does not do this. I left it because it does not traverse further back
             * than the Server root anyway.
             */
            if(request != "/"){
                fileWriter.write("[DIR] <a href='"+parentDirectory(request)+"'>Parent Directory/</a>\n");
            }
            /** 
             * this is where Kishori, dhanyu comes in. Thanks for the help and thanks Prof. 
             * Elliott for the reference. I have changed their code but they have influenced
             * the main Idea. It traverses all files and directories and creates a link
             * for the html file or directory
             */
            File[] files = f.listFiles();
            String name;
            int i = 0;
            while(i<files.length){
                name = files[i].toString().substring(2); // ommitting the "./"
                if(files[i].isDirectory()){
                    fileWriter.write("[DIR] "+"<a href='"+name+"'>"+name+"/</a>\n"); // writing a dir
                }
                else if(files[i].isFile() && !files[i].toString().equals("./send_site_MyWebServer.html")){
                    fileWriter.write("[TXT] "+"<a href='"+name+"'>"+name+"</a>\n"); // writing a file
                }
                i++;
            }
            fileWriter.write("</pre>\n");
            fileWriter.write("</html>\n"); // finishing off the file
            fileWriter.close(); // closing the writer

            /**
             * again this is where the actual file comes in handy
             * I can get the amount of bytes in the file and add it to the header
             * Also use my helper functions to send a correct header and the file 
             * itself.
             */
            long fileSize = toSend.length();
            sendHeader(out, "html", fileSize);
            sendFileContent("/send_site_MyWebServer.html", out);
            toSend.delete();
        }
        catch(IOException x){
            x.printStackTrace();
        }
    }

    /**
     * anserToRequest is the overview function that sorts through the various types of requets.
     * It prints the directory or file requested and handles whether a file or directory is 
     * searched for then accessing the actual functions needed for the handeling themselves.
     *  - send header
     *  - send file
     *  or 
     *  - display content
     * 
     * @param request is the location
     * @param PrintStream is the Browser connection
     */
    void answerToRequest(String request, PrintStream out){
        System.out.println(request);

        File current = new File("."+request);
        if(current.isFile()){ // if it is a file send the file
            String type = request.substring(request.indexOf("."));
            //now calculate data length
            long filesize = current.length();
            if(type!="html"){
                type = "plain";
            }
            sendHeader(out,type,filesize);
            sendFileContent(request, out);
        }
        if(current.isDirectory()){ // otherwise send the directory listing
            displayWebpage(request,out);
        }

    }

    /**
     * This is the function started when starting the Thread.
     * It is responsible for initializing the connection back to the server(Buffer and stream)
     * and recieves the input from the Browser to be able to respond to request.
     * This is the same as the InetServer and the JokeServer
     */
    public void run(){

        PrintStream out = null;
        BufferedReader in = null;

        try {

            // same handing as every server so far
            out = new PrintStream(sock.getOutputStream());
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String sockdata;

            sockdata = in.readLine ();
            String sub = sockdata.substring(4); // only here I pre edit the GET request and strip out the requested location sind the HTTP request is always the same there is no need for more handling
            sub = sub.substring(0,sub.indexOf(" "));

            // I catch the cgi requests earlier to access the addnums() function otherwise respond to usual requests
            // otherwise respond to the typical request.
            if(sub.contains("addnums.fake-cgi")){ 
                addnums(sub,out);
            }
            else{
                answerToRequest(sub,out);
            }
            sock.close();
        } 
        catch (IOException x){
            x.printStackTrace();
        }
    }
}



/**
 * This is the main WebServer class it is what handels the actual multithreaded
 * process. The main function starts and waits for incoming connections.
 * This server listens at port 2540 and is mean to answer HTML requests.
 * It can display the content of a folder traverse the folder structure
 * and display any text and html files (this includes java and any other text
 * format files). 
 * 
 * Go to BrowserWorker for a further understanding of the individual threaded
 * process.
 */
public class MyWebServer{


    public static void main(String[] args)throws IOException{ 
        int sim_conn = 6; 
        int port = 2540; 
        Socket sock; 

        ServerSocket serverSock = new ServerSocket(port,sim_conn);  

        System.out.println("Werner Reineke's WebServer server 4.2.666 starting up, listening at port 2540.\n");
        while(true){ 
            sock = serverSock.accept(); 
            new BrowseWorker(sock).start();
        }  
        
    }
}