
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
public class Main {
    static int i =12;
    static myClass mC=new myClass();
    Main(){
        i = 20;
    }
    public static void main(String[] args) throws IOException {
        //Semaphore
        int port = 55532;
        AtomicInteger counter = new AtomicInteger(0);
        ServerSocketChannel serverC = ServerSocketChannel.open();
        serverC.configureBlocking(false);
        serverC.bind(new InetSocketAddress(55533));
        Selector selector = Selector.open();
        serverC.register(selector, SelectionKey.OP_ACCEPT);
        while(true)
        {
            // 非阻塞监听注册事件
            if(selector.select(2000) == 0) {
                System.out.println("No selection");
                continue; }
            // 发现注册事件，
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
            while(keyIter.hasNext()) {
                SelectionKey key = keyIter.next();
                if(key.isAcceptable()) {
                    //通过ServerSocketChannel的accept()创建SocketChannel实例
                    //完成该操作意味着完成TCP三次握手，TCP物理链路正式建立
                    SocketChannel socketChannel = serverC.accept();
                    //设置为非阻塞
                    socketChannel.configureBlocking(false);
                    //在选择器注册，并订阅读事件
                    socketChannel.register(selector,SelectionKey.OP_READ);
                    System.out.println("Accepted");
                }
                if(key.isValid() && key.isReadable()) {
                    SocketChannel serverSocketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(4096);
                    int readBytes = serverSocketChannel.read(buffer);
                    if(readBytes < 0){
                        key.cancel();
                        //serverSocketChannel.close();
                        //return;
                        //serverSocketChannel.register(selector,SelectionKey.OP_READ);
                        //continue;
                    }
                    //读取到字节，对字节进行编解码
                    if(readBytes>0){
                        //将缓冲区从写模式切换到读模式
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);

                        String expression = new String(bytes,"UTF-8");
                        System.out.println("服务器收到消息："+expression + " counter:"+ (counter.addAndGet(1)));
                    }serverSocketChannel.register(selector,SelectionKey.OP_WRITE);
                }
                if(key.isValid() && key.isWritable()) {
                    SocketChannel socket = (SocketChannel) key.channel();
                    ByteBuffer buf = ByteBuffer.wrap(("你是第"+counter.get()+"个链接").getBytes());
                    System.out.println("Write: " + new String(buf.array(), 0, buf.limit()));
                    socket.write(buf);
                    buf.remaining();
                    if(!buf.hasRemaining()) {
                        // 取消写事件监听
                        key.interestOps(key.interestOps() &~  SelectionKey.OP_WRITE);
                    }
                    socket.register(selector,SelectionKey.OP_READ);
                    buf.compact();//socket.close();key.cancel();
                }keyIter.remove();
            }
        }
        /*
        ServerSocket server = new ServerSocket(port);
        // server将一直等待连接的到来
        System.out.println("server将一直等待连接的到来");
        server.setReuseAddress(true);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(128, 256,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
        AtomicInteger i = new AtomicInteger(0);
        while (true){
        Socket socket = server.accept();
        // 建立好连接后，从socket中获取输入流，并建立缓冲区进行读取
            Runnable runnable = ()->{
                try{
                    InputStream inputStream = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    int len;
                    StringBuilder sb = new StringBuilder();
                    while ((len = inputStream.read(bytes)) != -1) {
                        //注意指定编码格式，发送方和接收方一定要统一，建议使用UTF-8
                        sb.append(new String(bytes, 0, len,"UTF-8"));
                        }
                    System.out.println("get message from client: " + sb);
                    OutputStream outputStream=socket.getOutputStream();
                    outputStream.write((sb.toString().getBytes("UTF-8")));

                    System.out.println("return msg: "+i.addAndGet(1));
                    outputStream.close();
                    inputStream.close();
                    socket.close();
                }catch (Exception e){
                    System.out.println("error:");
                }
            };executor.execute(runnable);*/

    }
    private static void method(String s){
        System.out.println(s);
    }
}
class myClass{
    int i;
    myClass(){
        this.i=10;
    }
}