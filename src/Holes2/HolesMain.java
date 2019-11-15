/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Holes2;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import Holes2.HolesFrame;

public class HolesMain {
    public static void main(String[] args){
        EventQueue.invokeLater(() ->
        {
            JFrame frame = new HolesFrame();
            frame.setTitle("Holes");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        );
    }
}
