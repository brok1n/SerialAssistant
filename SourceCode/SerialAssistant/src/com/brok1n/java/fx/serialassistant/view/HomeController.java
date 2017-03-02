package com.brok1n.java.fx.serialassistant.view;

import com.brok1n.java.fx.serialassistant.AppDelegate;
import com.brok1n.java.fx.serialassistant.utils.Butils;
import gnu.io.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by brok1n on 2017/2/16.
 */
public class HomeController implements SerialPortEventListener {

    //串口连接超时时间
    public static final int CONNECT_SERIAL_TIMEOUT = 2000;

    //当前串口列表
    static Enumeration<CommPortIdentifier> portList;

    private SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");

    //串口列表
    @FXML
    private ComboBox serialComboBox;

    //波特率列表
    @FXML
    private ComboBox baudRateComboBox;

    //自定义波特率复选框
    @FXML
    private CheckBox customBaudRateCheckbox;

    //自定义波特率输入框
    @FXML
    private TextField customBaudRateTextField;

    //数据位列表
    @FXML
    private ComboBox dataBitComboBox;

    //校验位列表
    @FXML
    private ComboBox checkBitComboBox;

    //停止位列表
    @FXML
    private ComboBox stopBitComboBox;

    //状态LED
    @FXML
    private Circle statusLed;

    //打开、关闭 按钮
    @FXML
    private Button openCloseBtn;

    //状态栏 状态Label
    @FXML
    private Label statusLabel;

    //数据窗口
    @FXML
    private TextArea dataTextArea;

    //接收数据总数label
    @FXML
    private Label statusReceivedLabel;

    //发送数据总数label
    @FXML
    private Label statusSendLabel;

    //串口刷新文本按钮
    @FXML
    private Label refreshSerialListLabel;

    //接收转文件复选框
    @FXML
    private CheckBox receivedToFileCbox;

    //显示接收时间复选框
    @FXML
    private CheckBox showReceivedCbox;

    //显示十六进制复选框
    @FXML
    private CheckBox showHexCbox;

    //保存数据按钮
    @FXML
    private Button saveReceivedDataBtn;

    //发送按钮
    @FXML
    private Button sendBtn;

    //停止接收显示复选框
    @FXML
    private CheckBox stopShowReceivedDataCbox;

    //发送数据的TextArea
    @FXML
    private TextArea sendTextArea;

    //发送文件复选框
    @FXML
    private CheckBox sendFileCbox;

    //自动发送附加位
    @FXML
    private CheckBox autoSendAdditionalBitCbox;

    //发送完成自动清空
    @FXML
    private CheckBox sendCompleteAutoClearCbox;

    //以十六进制显示待发送数据
    @FXML
    private CheckBox showHexSendDataCbox;

    //数据流循环发送复选框
    @FXML
    private CheckBox dataStreamCyclicTransmissionCbox;

    //循环发送时间间隔
    @FXML
    private TextField intervalTimeTextField;

    //接收数据存储到的文件
    private File receivedToFile;

    //载入的文件
    private File loadFile;

    //要发送的文件
    private File sendFile;

    //串口是否被打开
    private boolean serialIsOpened;

    //当前连接的串口
    CommPortIdentifier currentSerial;

    //当前串口连接对象
    SerialPort currentSerialPort;

    //串口输入流
    InputStream currentSerialInputStream;

    //串口输出流
    OutputStream currentSerialOutputStream;

    //波特率 默认9600
    private int baudRate = 9600;

    //数据位 默认8
    private int dataBit = SerialPort.DATABITS_8;

    //校验位 默认无
    private int checkBit = SerialPort.PARITY_NONE;

    //停止位 默认1 1、1.5、2
    private int stopBit = SerialPort.STOPBITS_1;

    //串口发送数据总数
    private int serialSendDataCount;

    //串口接收数据总数
    private int serialReceivedDataCount;

    //是否显示接收时间
    private boolean showReceivedTime = false;

    //循环发送timer
    private Timer sendTimer;

    /**
     * 预分配1M空间给byte缓存
     */
    ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
    byte[] tmpByteArr = new byte[1024];


    public void init() {

        new Thread(new SerialRunnable()).start();

    }

    class SerialRunnable implements Runnable {

        @Override
        public void run() {
            portList = CommPortIdentifier.getPortIdentifiers();

            //初始化界面
            initView();

        }

        /**
         * 初始化界面
         */
        public void initView() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    //初始化串口列表
                    initSerialList();
                    serialComboBox.setVisibleRowCount(5);

                    //初始化波特率列表
                    baudRateComboBox.getItems().addAll("300", "600", "1200", "2400", "4800", "9600", "19200", "38400", "43000", "56000", "57600", "115200");
                    baudRateComboBox.setValue("9600");
                    baudRateComboBox.setVisibleRowCount(5);

                    //初始化禁用自定义波特率输入框
                    customBaudRateTextField.setDisable(true);

                    //初始化数据位列表
                    dataBitComboBox.getItems().addAll("5", "6", "7", "8");
                    dataBitComboBox.setValue("8");

                    /**
                     *  even 每个字节传送整个过程中bit为1的个数是偶数个（校验位调整个数）
                     2. odd 每个字节穿送整个过程中bit为1的个数是奇数个（校验位调整个数）
                     3. noparity没有校验位
                     4. space 校验位总为0
                     5. mark 校验位总为1
                     PARITY_NONE
                     PARITY_ODD
                     PARITY_EVEN
                     PARITY_MARK
                     PARITY_SPACE
                     * */
                    //初始化校验位列表
                    checkBitComboBox.getItems().addAll("none", "odd", "even", "mark", "space");
                    checkBitComboBox.setValue("none");

                    //初始化停止位列表
                    stopBitComboBox.getItems().addAll("1", "1.5", "2");
                    stopBitComboBox.setValue("1");

                    //关闭串口
                    closeSerial();

                }
            });
        }
    }

    /**
     * 初始化串口列表
     */
    public void initSerialList() {
        if (portList == null || serialComboBox == null) {
            return;
        }

        serialComboBox.getItems().clear();
        while (portList.hasMoreElements()) {
            CommPortIdentifier obj = (CommPortIdentifier) portList.nextElement();
            serialComboBox.getItems().add(obj.getName());
        }
    }

    /**
     * 打开/关闭 按钮被点击
     */
    @FXML
    public void onOpenCloseBtnClicked() {
        if (serialIsOpened) {
            closeSerial();
        } else {
            openSerial();
        }
    }

    /**
     * 关闭串口
     */
    public synchronized void closeSerial() {
        serialIsOpened = false;

        if (currentSerialPort != null) {
            try {
                currentSerialOutputStream.close();
                currentSerialInputStream.close();
                currentSerialPort.disableReceiveTimeout();
                currentSerialPort.disableReceiveFraming();
                currentSerialPort.disableReceiveThreshold();
                currentSerialPort.notifyOnDataAvailable(false);
                currentSerialPort.removeEventListener();
                currentSerialPort.close();
                currentSerialPort = null;
                currentSerialInputStream = null;
                currentSerialOutputStream = null;
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        statusLed.setStyle("-fx-fill: #FF0000; -fx-stroke: #FF0000");
        openCloseBtn.setText("打开串口");

        //可以修改当前串口参数
        serialComboBox.setDisable(false);
        refreshSerialListLabel.setDisable(false);
        baudRateComboBox.setDisable(customBaudRateCheckbox.isSelected());
        customBaudRateCheckbox.setDisable(false);
        customBaudRateTextField.setDisable(!customBaudRateCheckbox.isSelected());
        dataBitComboBox.setDisable(false);
        checkBitComboBox.setDisable(false);
        stopBitComboBox.setDisable(false);

    }

    /**
     * 打开串口
     */
    public void openSerial() {
        //获取当前选择的串口设备
        if (serialComboBox.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("请选择要连接的串口");

            alert.showAndWait();
            return;
        }
        String selectSerialName = serialComboBox.getValue().toString();
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier cpi = portList.nextElement();
            if (cpi != null && cpi.getName().equals(selectSerialName) && cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                currentSerial = cpi;
                break;
            }
        }

        //初始化串口参数
        baudRate = getBaudRate();
        dataBit = getDataBit();
        checkBit = getCheckBit();
        stopBit = getStopBit();

        //连接串口
        int status = -1;
        if ((status = connectSerial()) == 0) {
            serialIsOpened = true;

            //修改状态
            statusLed.setStyle("-fx-fill: #00FF00; -fx-stroke: #00FF00");
            openCloseBtn.setText("关闭串口");

            //连接成功禁止修改当前串口参数
            serialComboBox.setDisable(true);
            refreshSerialListLabel.setDisable(true);
            baudRateComboBox.setDisable(true);
            customBaudRateCheckbox.setDisable(true);
            customBaudRateTextField.setDisable(true);
            dataBitComboBox.setDisable(true);
            checkBitComboBox.setDisable(true);
            stopBitComboBox.setDisable(true);
        } else {
            closeSerial();
            if (status == 3) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("提示");
                alert.setHeaderText(null);
                alert.setContentText("串口被占用！");

                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("提示");
                alert.setHeaderText(null);
                alert.setContentText("串口打开失败！");

                alert.showAndWait();
            }
        }

    }

    /**
     * 建立串口连接
     */
    public int connectSerial() {
        //串口是否建立成功
        int status = -1;
        try {
            //建立串口连接、设置参数、监听器
            currentSerialPort = (SerialPort) currentSerial.open(currentSerial.getName(), HomeController.CONNECT_SERIAL_TIMEOUT);
            currentSerialPort.addEventListener(this);
            currentSerialPort.notifyOnDataAvailable(true);
                        /* 设置串口通讯参数 */
            currentSerialPort.setSerialPortParams(baudRate, dataBit, stopBit, checkBit);

            //串口输入输出流
            currentSerialInputStream = currentSerialPort.getInputStream();
            currentSerialOutputStream = currentSerialPort.getOutputStream();

            status = 0;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    String name = currentSerial.getName();
                    name = name.indexOf(File.separator) != -1 ? name.substring(name.lastIndexOf(File.separator) + 1) : name;
                    statusLabel.setText(name + " " + baudRate + " " + dataBitComboBox.getValue().toString() + " " + checkBitComboBox.getValue().toString() + " " + stopBitComboBox.getValue().toString());
                    statusLabel.setStyle("-fx-text-fill: #000000");
                }
            });

        } catch (TooManyListenersException e) {
            status = 1;
        } catch (UnsupportedCommOperationException e) {
            status = 2;
        } catch (PortInUseException e) {
            status = 3;
        } catch (IOException e) {
            status = 4;
        }
        return status;
    }

    /**
     * 获取波特率
     */
    public int getBaudRate() {
        return customBaudRateCheckbox.isSelected() ? Integer.parseInt(customBaudRateTextField.getText()) : Integer.parseInt(baudRateComboBox.getValue().toString());
    }

    /**
     * 获取数据位参数
     */
    public int getDataBit() {
        return Integer.parseInt(dataBitComboBox.getValue().toString());
    }

    /**
     * 获取当前的校验位
     * PARITY_NONE
     * PARITY_ODD
     * PARITY_EVEN
     * PARITY_MARK
     * PARITY_SPACE
     */
    public int getCheckBit() {
        int index = checkBitComboBox.getSelectionModel().getSelectedIndex();
        switch (index) {
            default:
            case 0:
                index = SerialPort.PARITY_NONE;
                break;
            case 1:
                index = SerialPort.PARITY_ODD;
                break;
            case 2:
                index = SerialPort.PARITY_EVEN;
                break;
            case 3:
                index = SerialPort.PARITY_MARK;
                break;
            case 4:
                index = SerialPort.PARITY_SPACE;
                break;
        }
        return index;
    }

    /**
     * 获取停止位
     */
    public int getStopBit() {
        int index = stopBitComboBox.getSelectionModel().getSelectedIndex();
        switch (index) {
            default:
            case 0:
                index = SerialPort.STOPBITS_1;
                break;
            case 1:
                index = SerialPort.STOPBITS_1_5;
                break;
            case 2:
                index = SerialPort.STOPBITS_2;
                break;
        }
        return index;
    }

    /**
     * 刷新串口
     */
    @FXML
    public void onRefreshLabelClicked() {
        //串口列表被点击 重新获取串口列表
        portList = CommPortIdentifier.getPortIdentifiers();
        initSerialList();
    }

    /**
     * 自定义波特率复选框被选中
     */
    @FXML
    public void onCustomBaudRateCboxClicked() {
        customBaudRateTextField.setDisable(!customBaudRateCheckbox.isSelected());
        baudRateComboBox.setDisable(customBaudRateCheckbox.isSelected());
    }

    /**
     * 清除显示按钮被点击
     */
    @FXML
    public void onClearTextAreaBtnClicked() {
        if (receivedToFileCbox != null && receivedToFileCbox.isSelected() && receivedToFile != null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("接收转向至文件时，无法执行该操作。");

            alert.showAndWait();
        } else {
            dataTextArea.setText("");
        }
    }

    /**
     * 发送接收统计清除按钮被点击
     */
    @FXML
    public void onClearRSCountBtnClicked() {
        serialReceivedDataCount = 0;
        serialSendDataCount = 0;
        statusReceivedLabel.setText("接收:" + serialReceivedDataCount);
        statusSendLabel.setText("发送:" + serialSendDataCount);
    }

    /**
     * 接收转文件复选框被点击
     */
    @FXML
    public void onReceivedToFileCboxClicked() {
        if (!receivedToFileCbox.isSelected()) {
            receivedToFileCbox.setSelected(false);
            dataTextArea.setText("");

            byteBuffer.clear();

            receivedToFile = null;
            dataTextArea.setDisable(false);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("接收区显示内容另存为...");
        File file = fileChooser.showSaveDialog(AppDelegate.static_stage);

        if (file == null) {
            receivedToFileCbox.setSelected(false);
            return;
        }

        boolean canCreate = true;
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                canCreate = true;
            } catch (IOException e) {
                e.printStackTrace();
                canCreate = false;
            }
        }

        //解除文件占用
        unlockFile(file);

        if (!canCreate || !file.canRead() || !file.canWrite()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("抱歉！您选择的文件无法读取或写入，请选择其他文件或使用管理员权限运行本程序。");

            alert.showAndWait();

            receivedToFileCbox.setSelected(false);

            return;
        }

        dataTextArea.clear();
        dataTextArea.setText("串口接收数据转向到文件：" + file.getAbsolutePath());

        byteBuffer.clear();

        receivedToFile = file;

        dataTextArea.setDisable(true);
    }

    /**
     * 保存数据按钮被点击
     */
    @FXML
    public void onSaveDataBtnClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存到...");
        File file = fileChooser.showSaveDialog(AppDelegate.static_stage);

        if (file == null) {
            receivedToFileCbox.setSelected(false);
            return;
        }

        //解除文件占用
        unlockFile(file);

        //统一换行符
        String data = dataTextArea.getText().replace("\r", "");
        data = data.replace("\n", "\r\n");

        //写入文件
        writeToFile(data, file);

    }

    /**
     * 发送按钮被点击
     */
    @FXML
    public void onSendBtnClicked() {

        //检测是否已经连接到串口 没有连接就不让发送
        if( currentSerialPort == null || currentSerial == null || currentSerialOutputStream == null || currentSerialInputStream == null )
        {
            Alert alert = new Alert(Alert.AlertType.WARNING );
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("未连接到串口");
            alert.showAndWait();
            return ;
        }

        //是否是循环发送
        if( dataStreamCyclicTransmissionCbox != null && dataStreamCyclicTransmissionCbox.isSelected()  )
        {
            Object object = sendBtn.getUserData();
            if( object == null )
            {
                //开始循环发送
                sendBtn.setUserData("1");
                sendBtn.setText("停止发送");
                openCloseBtn.setDisable(true);
                int offsetTime = Integer.parseInt(intervalTimeTextField.getText());
                sendTimer = new Timer();
                sendTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendData();
                    }
                }, 0, offsetTime);

            }
            else
            {
                //结束循环发送
                sendBtn.setUserData(null);
                sendBtn.setText("发送");
                openCloseBtn.setDisable(false);
                if( sendTimer != null )
                {
                    sendTimer.cancel();
                }
            }
        }
        else
        {
            sendData();
        }

        //发送完清空被
        if( sendTextArea != null && sendCompleteAutoClearCbox != null && sendCompleteAutoClearCbox.isSelected() )
        {
            sendTextArea.setText("");
        }
    }

    /**
     * 载入文件按钮被点击
     */
    @FXML
    public void onLoadFileBtnClicked() {

        if( sendFileCbox != null && sendFileCbox.isSelected() )
        {
            Alert alert = new Alert(Alert.AlertType.WARNING );
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("启用外部数据源时无法执行该操作");
            alert.showAndWait();

            return ;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开");
        loadFile = fileChooser.showOpenDialog(AppDelegate.static_stage);

        if (loadFile == null) {
            return;
        }

        //先读取1024个字节用作临时显示
        byte[] buffer = new byte[2048];
        int len = readFile( loadFile, buffer );

        if( len < 0 )
        {
            Alert alert = new Alert(Alert.AlertType.WARNING );
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("载入了一个空文件或该文件无法读取");
            alert.showAndWait();
            return;
        }

        //是否为文本文件并且用户是否选择了以16进制形式载入
        String sendDataTmp = "";
        if( !isTextFile(buffer, len) && isLoadHex() )
        {
            sendDataTmp =  Butils.bytes2HexString(buffer, 0, len);
            String regex = "(.{2})";
            sendDataTmp = sendDataTmp.replaceAll (regex, "$1 ");
            showHexSendDataCbox.setSelected(true);
        }
        else
        {
            sendDataTmp = new String( buffer, 0, len);
            showHexSendDataCbox.setSelected(false);
        }

        sendTextArea.setText( "" + sendDataTmp );
        sendTextArea.setEditable(false);
    }


    /**
     * 判断文件是否为文本文件
     * */
    private boolean isTextFile( byte[] buffer, int len )
    {
        String tmp = new String( buffer, 0, len );
        byte[] tmplen = tmp.getBytes();
        if( len == tmplen.length )
        {
            return true;
        }
        return false;
    }

    /**
     * 是否以16进制方式载入文件
     * */
    private boolean isLoadHex()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "是否以十六进制数据载入？",new ButtonType("取消", ButtonBar.ButtonData.NO),
                new ButtonType("确定", ButtonBar.ButtonData.YES));
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.initOwner( AppDelegate.static_stage );

        Optional<ButtonType> bt =  alert.showAndWait();

        //根据点击结果返回
        if(bt.get().getButtonData().equals(ButtonBar.ButtonData.YES)){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 读取文件
     * */
    private int readFile( File file, byte[] buffer )
    {
        //首先解锁文件
        unlockFile( file );

        try {
            FileInputStream inputStream = new FileInputStream( file );
            int len = inputStream.read(buffer);
            return len;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    };


    /**
     * 清空发送区按钮被点击
     */
    @FXML
    public void onClearSendAreaBtnClicked() {

        if( sendFileCbox != null && sendFileCbox.isSelected() )
        {
            Alert alert = new Alert(Alert.AlertType.WARNING );
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("启用外部数据源时无法执行该操作");
            alert.showAndWait();
        }
        else if (sendTextArea != null) {
            sendTextArea.setText("");
            sendTextArea.setEditable(true);
            showHexSendDataCbox.setSelected(false);
            sendFile = null;
            loadFile = null;
        }
    }

    /**
     * 发送文件复选框被点击
     */
    @FXML
    public void onSendFileCboxClicked() {

        if( sendFileCbox != null && !sendFileCbox.isSelected() )
        {
            sendFile = null;
            sendTextArea.setText("");
            sendTextArea.setDisable(false);
            showHexSendDataCbox.setDisable(false);
            sendCompleteAutoClearCbox.setDisable(false);
            return ;
        }

        //选择文件
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开");
        sendFile = fileChooser.showOpenDialog(AppDelegate.static_stage);

        if (sendFile == null) {
            return;
        }

        //修改发送区域显示内容 禁止发送区可修改
        sendTextArea.setText("启用外部文件数据源...\r\n" + sendFile.getAbsolutePath() );
        sendTextArea.setDisable(true);

        showHexSendDataCbox.setSelected(false);
        showHexSendDataCbox.setDisable(true);

        sendCompleteAutoClearCbox.setSelected(false);
        sendCompleteAutoClearCbox.setDisable(true);

    }

    /**
     * 以十六进制显示要发送的数据复选框被点击
     * */
    @FXML
    public void showHexSendDataCboxClicked()
    {
        if( showHexSendDataCbox != null && showHexSendDataCbox.isSelected() )
        {
            String tmpData = sendTextArea.getText();
            tmpData = Butils.bytes2HexString( tmpData.getBytes() );
            String regex = "(.{2})";
            tmpData = tmpData.replaceAll (regex, "$1 ");
            sendTextArea.setText( tmpData );
        }
        else
        {
            String tmpData = sendTextArea.getText();
            tmpData = Butils.hexStringToString( tmpData.replace(" ", "") );
            sendTextArea.setText( tmpData );
        }
    }

    /**
     * 向串口发送数据
     * */
    private synchronized boolean sendDataToSerial( byte[] data, int len) {
        boolean status = false;
        if (currentSerialOutputStream != null) {
            try {
                if( len > 0 )
                {
                    currentSerialOutputStream.write(data, 0, len);
                }
                else
                {
                    currentSerialOutputStream.write(data);
                }
                currentSerialOutputStream.flush();
                status = true;
            } catch (IOException e) {
                status = false;
            }
        }
        return status;
    }

    /**
     * 发送文件给串口
     * */
    private synchronized void sendFileToSerial(File file)
    {
        if( file == null )
        {
            return ;
        }

        //解锁文件
        unlockFile( file );

        byte[] tmp = new byte[1024];
        try {
            FileInputStream inputStream = new FileInputStream( file );
            while ( inputStream.available() > 0 )
            {
                int len = inputStream.read( tmp );
                sendDataToSerial( tmp, len );
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    /**
     * 发送数据
     * */
    private void sendData()
    {
        //如果是发送文件
        if( sendFileCbox != null && sendFileCbox.isSelected() )
        {
            sendFileToSerial( sendFile );
            return ;
        }

        //如果是发送载入的文件
        if( sendTextArea != null && !sendTextArea.isEditable() && loadFile != null )
        {
            sendFileToSerial( loadFile );
            return ;
        }

        //发送普通字符串数据
        String dataTmp = "";
        if( sendTextArea != null && showHexSendDataCbox != null && showHexSendDataCbox.isSelected() )
        {
            dataTmp = sendTextArea.getText();
            dataTmp = Butils.hexStringToString( dataTmp.replace(" ", "") );
        }
        else if( sendTextArea != null )
        {
            dataTmp = sendTextArea.getText();
        }

        sendDataToSerial( dataTmp.getBytes(), 0 );
    }

    @Override
    public void serialEvent(SerialPortEvent event)
    {
        switch (event.getEventType()) {
            case SerialPortEvent.BI: // 10 通讯中断
                break;
            case SerialPortEvent.OE: // 7 溢位（溢出）错误
            case SerialPortEvent.FE: // 9 帧错误
            case SerialPortEvent.PE: // 8 奇偶校验错误
            case SerialPortEvent.CD: // 6 载波检测
            case SerialPortEvent.CTS: // 3 清除待发送数据
            case SerialPortEvent.DSR: // 4 待发送数据准备好了
            case SerialPortEvent.RI: // 5 振铃指示
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
                break;
            case SerialPortEvent.DATA_AVAILABLE:// 1 串口存在可用数据

                int len = receivedData();
                receivedOutput( byteBuffer, len );
                byteBuffer.clear();

                break;
            default:
                break;
        }
    }

    /**
     * 接收数据
     * */
    private int receivedData()
    {
        int len, count = 0;
        try {
            while ( currentSerialInputStream.available() > 0 )
            {
                len = currentSerialInputStream.read( tmpByteArr );
                byteBuffer.put( tmpByteArr, 0, len );
                count += len;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 输出接收
     * */
    private void receivedOutput( ByteBuffer data, int len )
    {
        //处理暂停接收显示、16进制、是否显示接收时间
        String result = prepareReceivedData( data, len );

        //是打印到界面还是存到文件
        if( receivedToFileCbox != null && receivedToFileCbox.isSelected() && receivedToFile != null )
        {
            writeToFile( result, receivedToFile );
        }
        else
        {
            writeToView( result );
        }

        //接收总数据增加
        serialReceivedDataCount += len;

        //刷新状态信息
        flushStatus();

    }

    //刷新状态信息
    private void flushStatus()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int point = dataTextArea.getCaretPosition();
                if( point > 4000 )
                {
                    dataTextArea.setText( dataTextArea.getText().substring(point - 4000 ));
                    //dataTextArea.selectPositionCaret( dataTextArea.getText().length());
                    dataTextArea.positionCaret(dataTextArea.getText().length());
                }
                statusReceivedLabel.setText("接收:" + serialReceivedDataCount );
            }
        });
    }

    //写入到视图中
    private void writeToView( final String data )
    {
        if( dataTextArea != null )
        {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    dataTextArea.appendText( data );
                }
            });
        }
    }

    //写入文件
    private void writeToFile( String data, File file )
    {
        try {
            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file, true )) );
            writer.write(data);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //解除文件占用
    private void unlockFile( File file )
    {
        try {
            RandomAccessFile  raf  = new RandomAccessFile( file, "rw" );
            FileLock fl = raf.getChannel().tryLock();
            Thread.sleep(100);
            fl.release();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //处理接收到的数据
    private String prepareReceivedData( ByteBuffer data, int len )
    {
        String result = "";
        //如果停止接收显示被选中 就不输出接收到的数据
        if( stopShowReceivedDataCbox != null && stopShowReceivedDataCbox.isSelected() )
        {
            return result;
        }

        //是否以16进制格式显示
        if( showHexCbox != null && showHexCbox.isSelected() )
        {
            result =  Butils.bytes2HexString(data.array(), 0, len);
            String regex = "(.{2})";
            result = result.replaceAll (regex, "$1 ");
        }
        else
        {
            result = new String( data.array(), 0, len);
        }

        //是否显示接收时间
        if( showReceivedCbox != null && showReceivedCbox.isSelected() )
        {
            result = "\r\n[" + sd.format(new Date()) + "]" + result;
        }

        return result;
    }

}
