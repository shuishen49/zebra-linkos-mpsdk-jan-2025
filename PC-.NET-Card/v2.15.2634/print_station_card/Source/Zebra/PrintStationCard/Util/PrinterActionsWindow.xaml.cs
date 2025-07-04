/***********************************************
 * CONFIDENTIAL AND PROPRIETARY 
 * 
 * The source code and other information contained herein is the confidential and exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published, 
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 * 
 * Copyright ZIH Corp. 2018
 * 
 * ALL RIGHTS RESERVED
 ***********************************************/

using System;
using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Animation;

namespace Zebra.PrintStationCard.Util {

    /// <summary>
    /// Interaction logic for PrinterActionsWindow.xaml
    /// </summary>
    public partial class PrinterActionsWindow : Window {

        private string actionLabelText;

        public PrinterActionsWindow(string actionText) {
            actionLabelText = actionText;
            InitializeComponent();
        }

        private void Window_Loaded(object sender, RoutedEventArgs e) {
            action_Label.Content = actionLabelText;

            DoubleAnimation da = new DoubleAnimation() {
                From = 0,
                To = 360,
                Duration = new Duration(TimeSpan.FromSeconds(1.5)),
                RepeatBehavior = RepeatBehavior.Forever
            };

            RotateTransform rt = new RotateTransform();
            animatedArrow_Image.RenderTransform = rt;
            animatedArrow_Image.RenderTransformOrigin = new Point(0.5, 0.5);
            rt.BeginAnimation(RotateTransform.AngleProperty, da);
        }

        private void Window_MouseLeftButtonDown(object sender, System.Windows.Input.MouseButtonEventArgs e) {
            DragMove();
        }
    }
}
