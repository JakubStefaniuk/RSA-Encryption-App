/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsaapp;

import javax.swing.JFrame;
import rsaapp.RSA.TwoThreeTuple;

/**
 *
 * @author Joanna
 */
public class RSAapp extends JFrame {
    public RSAapp(){
        initComponents();
    }
    public void initComponents(){
        
    }
    public static void main(String[] args) {
       // new RSAapp().setVisible(true);
       //testing...
       TwoThreeTuple a = RSA.generateKeys();
       String enc = RSA.encrypt("aaaaaaaaaaaaaaaa", (long)a.getSecond(), (long)a.getThird());
       System.out.println(enc);
       String msg = RSA.decrypt(enc, (long)a.getFirst(), (long)a.getThird());
        System.out.println(msg);
    }
    
}
