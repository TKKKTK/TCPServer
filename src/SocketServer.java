import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class SocketServer {

    BufferedWriter writer = null;
    BufferedReader bufferedReader = null;
    public static void main(String[] args) {
        TCPServer();
    }
    public static void TCPServer(){
        try {
            //创建服务器端 Socket，指定监听端口
            ServerSocket serverSocket = new ServerSocket(8888);
            //等待客户端连接
            Socket clientSocket = serverSocket.accept();

            System.out.println("连接成功!!");
            //获取客户端输入流，
            InputStream is = clientSocket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String data = null;
            //读取客户端数据
            while((data = br.readLine()) != null){
                System.out.println("服务器接收到客户端的数据：" + data);
            }
            //关闭输入流
            clientSocket.shutdownInput();
            //获取客户端输出流
            OutputStream os = clientSocket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            //向客户端发送数据
            pw.print("服务器给客户端回应的数据");
            pw.flush();
            //关闭输出流
            clientSocket.shutdownOutput();
            //关闭资源
            pw.checkError();
            os.close();
            br.close();
            isr.close();
            is.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
