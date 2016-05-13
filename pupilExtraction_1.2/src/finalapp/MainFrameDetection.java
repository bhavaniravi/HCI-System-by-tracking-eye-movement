package finalapp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;

/**
 *@see MainFrameDetection
 * @version 1.0
 * Main class which generates a frame 
 * Creates a daemon thread for eye capturing and tracking
 * @author BHAVANI
 */
public class MainFrameDetection extends javax.swing.JFrame {
    /**daemon thread object*/
    private DaemonThread myThread = null;
     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int count = 0;
    /**camera object*/
    VideoCapture webSource = null;
    /**Matrix holds captured frames*/
 static boolean position=true,calibBool=true,cCenter=true,cULeft=true,cURight=true,cBLeft=true,cBRight=true,topMid=true,leftMid=true,rightMid=true,botMid=true;
   /**
 *@see DaemonThread
 * @version 1.0
 * Daemon thread class for eye tracking 
 * @author BHAVANI
 */
    class DaemonThread implements Runnable {
        
        protected volatile boolean runnable = false;
        
        @Override
        public void run() {
            synchronized (this) {
              
                while (true) {
                    //object of pupil detection class --> control transfers to constructor
                    Graphics g = jPanel1.getGraphics();  
                     EyeGaze pu= new EyeGaze();
//                if(position){
//                    long a= System.currentTimeMillis(); 
//                    long b;
//                    while(true){
//                      b= System.currentTimeMillis();
//                     if(b-a>=3000){
//                            
//                               break;
//                               }
//                   
//                     g.setFont(new Font("TimesRoman", Font.PLAIN, 72)); 
//                     g.drawString("Position your face such that pupil is detected", 300, 300);
//                   }
//                   
//                    a= System.currentTimeMillis(); 
//                  
//                while(true){
//                    b= System.currentTimeMillis(); 
//                     calibBool=false;
//                     Image im=pu.processFrame(webSource,false,0);
//                     BufferedImage buff = (BufferedImage) im;
//                     g.drawImage(buff, 0, 0, getWidth(), getHeight()-150 , 0, 0, buff.getWidth(), buff.getHeight(), null);
//                     if(b-a>=10000){
//                         calibBool=true;
//                               break;
//                               }
//                   }
//                position=false;
//                }
//                    if(calibBool){
//                    Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
//                       
//                    calib(cCenter, 1, g, pu);
//                    cCenter=false;                    
//                    }
                    calibBool=false;
                   
                    //tranfer the camera object to fetch and process each frame
                     Image im=pu.processFrame(webSource,false,10);
                     BufferedImage buff = (BufferedImage) im;
                     g.drawImage(buff, 0, 0, getWidth(), getHeight()-150 , 0, 0, buff.getWidth(), buff.getHeight(), null);
//              
            }}
        }
    
    }
    void calib(boolean bool,int method,Graphics g,EyeGaze pu){
        int count=0;
         long a= System.currentTimeMillis(); 
         long b;
                while(bool){
                              count++;
                              see.setText(new Integer(method).toString());
                              b= System.currentTimeMillis(); 
                             
                               if(b-a>=3000){
                               bool=false;
                               break;
                               }
                    
                           g.setColor(this.getBackground());//set the color you want to clear the bound this point to JFrame/JPanel
                            g.fillRect(0, 0, jPanel1.getWidth(), jPanel1.getHeight());
                            g.setColor(Color.red);
                            g.fillOval(screenSize.width/2-70, screenSize.height/2-70, 70, 70);
                            pu.processFrame(webSource,true,method);
                        
                             
                         }
              
              //  pu.average(count+1,method);
    }
    public MainFrameDetection() {
        initComponents();
       
        setBounds(0,0,screenSize.width, screenSize.height-50);
      
      
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        see = new javax.swing.JLabel();
        calibrate = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 574, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 359, Short.MAX_VALUE)
        );

        jButton1.setText("Start");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Pause");

        see.setText("jLabel1");

        calibrate.setText("Calibrate");
        calibrate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addComponent(jButton1)
                .addGap(34, 34, 34)
                .addComponent(see)
                .addGap(65, 65, 65)
                .addComponent(calibrate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addGap(69, 69, 69))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1)
                        .addComponent(jButton2)
                        .addComponent(calibrate))
                    .addComponent(see, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
       
      
        webSource = new VideoCapture(0); // video capture from default cam
        myThread = new MainFrameDetection.DaemonThread(); //create object of thread class
        Thread t = new Thread(myThread);
        t.setDaemon(true);
        myThread.runnable = true;
        t.start();                 //start thrad
        jButton1.setEnabled(false);  // deactivate start button
        jButton2.setEnabled(true);  //  activate stop button
    }//GEN-LAST:event_jButton1ActionPerformed
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
       
        jButton2.setEnabled(false);   // activate start button 
        jButton1.setEnabled(true);     // deactivate stop button
        webSource.release();  // stop caturing fron cam
         myThread.runnable = false;            // stop thread

    }//GEN-LAST:event_jButton2ActionPerformed

    private void calibrateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrateActionPerformed
        // TODO add your handling code here:
        cCenter=true;cULeft=true;cURight=true;cBLeft=true;cBRight=true;calibBool=true;
    }//GEN-LAST:event_calibrateActionPerformed
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrameDetection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrameDetection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrameDetection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrameDetection.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                 
                new MainFrameDetection().setVisible(true);
                new OptFrame();
            }
        });
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton calibrate;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    public javax.swing.JPanel jPanel1;
    private javax.swing.JLabel see;
    // End of variables declaration//GEN-END:variables

}
