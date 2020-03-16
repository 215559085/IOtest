import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class TimerT extends TimerTask{
    SocketChannel socket;
    ByteBuffer byteBuffer = ByteBuffer.wrap("Timer Heart Beat".getBytes());
    public TimerT(SocketChannel s){
        this.socket=s;
    }
    @Override
    public void run() {
        try {
            System.out.println("HeartBeat\n");
            socket.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 要连接的服务端IP地址和端口
        String host = "127.0.0.1";
        int port = 6666;
        // 与服务端建立连接
        int count = 0;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2048,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(true));//or
        ReentrantLock lock = new ReentrantLock();
                    int i=0;
                    while(++i < 2048){
                    Runnable runnable = ()-> {
                            lock.tryLock();
                        try {
                            SocketChannel socket = SocketChannel.open();
                            socket.configureBlocking(false);
                            Selector selector = Selector.open();
                            //注册连接事件
                            socket.register(selector, SelectionKey.OP_CONNECT);
                            //发起连接
                            boolean online = false;
                            socket.connect(new InetSocketAddress("127.0.0.1", port));
                            while(true){
                                if(socket.isOpen()){
                                    selector.select();
                                    Iterator<SelectionKey> keys=selector.selectedKeys().iterator();
                                    while(keys.hasNext()){
                                    //Thread.sleep(100L);
                                        SelectionKey key = keys.next();
                                        keys.remove();
                                        if(online){
                                        socket.write(ByteBuffer.wrap(("Rewrite").getBytes()));
                                        System.out.println("ReWrite");
                                    }
                                    if(key.isConnectable()){
                                        while (!socket.finishConnect()) {
                                            System.out.println("连接中");
                                        }
                                        Timer timer = new Timer();
                                        timer.schedule(new TimerT(socket),1L);//心跳write
                                        socket.register(selector,SelectionKey.OP_WRITE);
                                    }if(key.isReadable()){
                                        System.out.println("Reading...");
                                        SocketChannel ssc =(SocketChannel) key.channel();
                                        ByteBuffer bf = ByteBuffer.allocate(2048);
                                        int len;
                                        if((len = ssc.read(bf)) > 0){
                                            String msg = new String(bf.array(),0,len);
                                            System.out.println("Read :"+msg);
                                        }else{
                                            key.cancel();
                                            socket.close();
                                        }socket.register(selector,SelectionKey.OP_WRITE);
                                    }if(key.isWritable()){
                                        SocketChannel ssc = (SocketChannel)key.channel();
                                        System.out.println("Writing: 你好");
                                        ssc.write(ByteBuffer.wrap("你好".getBytes()));
                                        //key.cancel();
                                        socket.register(selector,SelectionKey.OP_READ);
                                    }
                                }
                            }
                        } }catch (Exception e){
                            e.printStackTrace();
                        }
                       };executor.execute(runnable);
                    }
                /*Socket socket = new Socket(host, port);
                // 建立连接后获得输出流
                OutputStream outputStream = socket.getOutputStream();
                String message = "你好";
                outputStream.write(message.getBytes("UTF-8"));
                socket.shutdownOutput();
                /*InputStream inputStream = socket.getInputStream();
                byte[] bytes = new byte[1024];
                int len;
                StringBuilder sb = new StringBuilder();
                while ((len = inputStream.read(bytes)) != -1) {
                    //注意指定编码格式，发送方和接收方一定要统一，建议使用UTF-8
                    sb.append(new String(bytes, 0, len, "UTF-8"));
                }
                System.out.println("receive msg:" + sb);
                outputStream.close();
                //inputStream.close();
                //socket.close();*/
            }//;executor.execute(runnable);
}
