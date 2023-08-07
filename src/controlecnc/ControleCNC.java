/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controlecnc;

import arduinocontroll.PanelControl;
import dist.PanelGCode;
import dist.gcode.ArquivoGCode;
import dist.serial.ReadEventSerial;
import dist.serial.ReadWriteSerial;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 *
 * @author rodri
 */
public class ControleCNC extends JFrame{

    private PanelGCode panel_gcode;
    private PanelControl panel_control;
    
    private JButton bt_run;
    private JButton bt_pause;
    private JButton bt_cancel;
    private JProgressBar progress;
    
    private final int RUN= 1;
    private final int PAUSE= 2;
    private final int CANCEL= 3;
    private final int OSIOSO= 0;
    private int acao_comando= 0;
    private int linha_executar;
    
    public ControleCNC(){
        acao_comando= 0;
        linha_executar= 0;
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        
        this.setTitle("Controle CNC");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel= new JPanel();
        panel.setLayout(new GridLayout(1, 2));
        panel_gcode= new PanelGCode();
        panel.add(panel_gcode);
        
        panel_control= new PanelControl(new EventSerial());
        
        bt_run= panel_control.getBt_run();
        bt_run.addActionListener((ActionEvent ae) -> {
            if(ArquivoGCode.isFileOpen()){
                linha_executar= (acao_comando == PAUSE) ? linha_executar:0;
                acao_comando= RUN;
                panel_control.desativar_botoes();
                bt_run.setEnabled(false);
                bt_pause.setEnabled(true);
                bt_cancel.setEnabled(true);
                
                panel_control.append_area_texto("Executando arquivo: "+ArquivoGCode.getPathFile()+'\n');
                panel_control.append_area_texto("==> G92\n");
                ReadWriteSerial.enviaDados("G92");
            }else
                JOptionPane.showMessageDialog(null, "Nenhum arquivo G aberto");
        });
        
        bt_pause= panel_control.getBt_pause();
        bt_pause.addActionListener((ActionEvent ae) -> {
            acao_comando= PAUSE;
            panel_control.ativar_botoes();
            panel_control.append_area_texto("Arquivo de comando em pausa!\n");
            //ReadWriteSerial.enviaDados("PAUSAR");
            JOptionPane.showMessageDialog(null, "Arquivo de comando pausado!");
        });
        
        bt_cancel= panel_control.getBt_cancel();
        bt_cancel.addActionListener((ActionEvent ae) -> {
            acao_comando= OSIOSO;
            panel_control.append_area_texto("Arquivo de comando cancelado!\n");
            //ReadWriteSerial.enviaDados("CANCELAR");
            JOptionPane.showMessageDialog(null, "Arquivo de comando cancelado!");
        });
        
        panel.add(panel_control);
        
        this.getContentPane().add(BorderLayout.CENTER, panel);
        
        /*this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        
        this.getContentPane().add( panel_gcode);
        this.getContentPane().add(panel_control);*/
        
        this.setSize(1200, 800);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
    
    class EventSerial implements ReadEventSerial{

        @Override
        public void readEvent(String data) {
            switch(acao_comando){
                case OSIOSO:
                    //if(data.equals("ok"))
                        panel_control.ativar_botoes();
                    break;
                case RUN:
                    if(linha_executar < ArquivoGCode.getNumLinhas()){
                        String comand= ArquivoGCode.getLinhaComando(linha_executar);
                        panel_control.append_area_texto("==> "+comand+'\n');
                        ReadWriteSerial.enviaDados(comand);
                        linha_executar++;
                    }else{
                        acao_comando= OSIOSO;
                        panel_control.ativar_botoes();
                        linha_executar= 0;
                        JOptionPane.showMessageDialog(null, "Arquivo de comandos finalizado!");
                    }
                    break;
                case PAUSE:
                    //if(data.equals("ok"))
                        panel_control.ativar_botoes();
                    break;
            }
        }
        
    }
    
    public static void main(String[] args) {
        new ControleCNC();
    }
}
