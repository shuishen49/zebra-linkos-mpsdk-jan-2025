namespace ZCSCDevDemo {

    partial class FormMain {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing) {
            if (disposing && (components != null)) {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent() {
            this.cboCardType = new System.Windows.Forms.ComboBox();
            this.lblTestTypeBasic = new System.Windows.Forms.Label();
            this.btnExit = new System.Windows.Forms.Button();
            this.btnTest = new System.Windows.Forms.Button();
            this.lblMsg = new System.Windows.Forms.Label();
            this.cbTestPrint = new System.Windows.Forms.CheckBox();
            this.cboCardGroup = new System.Windows.Forms.ComboBox();
            this.lblTestPrint = new System.Windows.Forms.Label();
            this.lblCardType = new System.Windows.Forms.Label();
            this.lblBannerText = new System.Windows.Forms.Label();
            this.lblReaderLabel = new System.Windows.Forms.Label();
            this.lblBanner = new System.Windows.Forms.Label();
            this.gbButtons = new System.Windows.Forms.GroupBox();
            this.lblReader = new System.Windows.Forms.Label();
            this.lblVersion = new System.Windows.Forms.Label();
            this.gbReaderCard = new System.Windows.Forms.GroupBox();
            this.nbOffset = new System.Windows.Forms.NumericUpDown();
            this.lblOffset = new System.Windows.Forms.Label();
            this.lblReaderVersion = new System.Windows.Forms.Label();
            this.pbBannerLogo = new System.Windows.Forms.PictureBox();
            this.gbButtons.SuspendLayout();
            this.gbReaderCard.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.nbOffset)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pbBannerLogo)).BeginInit();
            this.SuspendLayout();
            // 
            // cboCardType
            // 
            this.cboCardType.FormattingEnabled = true;
            this.cboCardType.Location = new System.Drawing.Point(130, 100);
            this.cboCardType.Name = "cboCardType";
            this.cboCardType.Size = new System.Drawing.Size(300, 24);
            this.cboCardType.TabIndex = 2;
            // 
            // lblTestTypeBasic
            // 
            this.lblTestTypeBasic.AutoSize = true;
            this.lblTestTypeBasic.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblTestTypeBasic.ForeColor = System.Drawing.Color.Black;
            this.lblTestTypeBasic.Location = new System.Drawing.Point(10, 100);
            this.lblTestTypeBasic.Name = "lblTestTypeBasic";
            this.lblTestTypeBasic.Size = new System.Drawing.Size(110, 24);
            this.lblTestTypeBasic.TabIndex = 13;
            this.lblTestTypeBasic.Text = "Test Type:";
            // 
            // btnExit
            // 
            this.btnExit.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnExit.Location = new System.Drawing.Point(900, 20);
            this.btnExit.Name = "btnExit";
            this.btnExit.Size = new System.Drawing.Size(100, 30);
            this.btnExit.TabIndex = 6;
            this.btnExit.Text = "Exit";
            this.btnExit.UseVisualStyleBackColor = true;
            this.btnExit.Click += new System.EventHandler(this.BtnExit_Click);
            // 
            // btnTest
            // 
            this.btnTest.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnTest.Location = new System.Drawing.Point(790, 20);
            this.btnTest.Name = "btnTest";
            this.btnTest.Size = new System.Drawing.Size(100, 30);
            this.btnTest.TabIndex = 5;
            this.btnTest.Text = "Run Test";
            this.btnTest.UseVisualStyleBackColor = true;
            this.btnTest.Click += new System.EventHandler(this.BtnTest_Click);
            // 
            // lblMsg
            // 
            this.lblMsg.Font = new System.Drawing.Font("Arial", 10F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblMsg.ForeColor = System.Drawing.Color.DarkRed;
            this.lblMsg.Location = new System.Drawing.Point(20, 20);
            this.lblMsg.Name = "lblMsg";
            this.lblMsg.Size = new System.Drawing.Size(772, 23);
            this.lblMsg.TabIndex = 19;
            this.lblMsg.Text = "Status messages go here";
            this.lblMsg.Visible = false;
            // 
            // cbTestPrint
            // 
            this.cbTestPrint.AutoSize = true;
            this.cbTestPrint.Location = new System.Drawing.Point(565, 64);
            this.cbTestPrint.Name = "cbTestPrint";
            this.cbTestPrint.Size = new System.Drawing.Size(18, 17);
            this.cbTestPrint.TabIndex = 5;
            this.cbTestPrint.UseVisualStyleBackColor = true;
            // 
            // cboCardGroup
            // 
            this.cboCardGroup.Font = new System.Drawing.Font("Arial", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.cboCardGroup.FormattingEnabled = true;
            this.cboCardGroup.Location = new System.Drawing.Point(130, 60);
            this.cboCardGroup.Name = "cboCardGroup";
            this.cboCardGroup.Size = new System.Drawing.Size(300, 25);
            this.cboCardGroup.TabIndex = 1;
            this.cboCardGroup.SelectedIndexChanged += new System.EventHandler(this.CboCardGroup_SelectedIndexChanged);
            // 
            // lblTestPrint
            // 
            this.lblTestPrint.AutoSize = true;
            this.lblTestPrint.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblTestPrint.ForeColor = System.Drawing.Color.Black;
            this.lblTestPrint.Location = new System.Drawing.Point(450, 60);
            this.lblTestPrint.Name = "lblTestPrint";
            this.lblTestPrint.Size = new System.Drawing.Size(109, 24);
            this.lblTestPrint.TabIndex = 29;
            this.lblTestPrint.Text = "Test Print:";
            // 
            // lblCardType
            // 
            this.lblCardType.AutoSize = true;
            this.lblCardType.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblCardType.ForeColor = System.Drawing.Color.Black;
            this.lblCardType.Location = new System.Drawing.Point(10, 60);
            this.lblCardType.Name = "lblCardType";
            this.lblCardType.Size = new System.Drawing.Size(114, 24);
            this.lblCardType.TabIndex = 18;
            this.lblCardType.Text = "Card Type:";
            // 
            // lblBannerText
            // 
            this.lblBannerText.AutoSize = true;
            this.lblBannerText.BackColor = System.Drawing.Color.Black;
            this.lblBannerText.Font = new System.Drawing.Font("Arial", 20F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblBannerText.ForeColor = System.Drawing.Color.White;
            this.lblBannerText.Location = new System.Drawing.Point(20, 28);
            this.lblBannerText.Name = "lblBannerText";
            this.lblBannerText.Size = new System.Drawing.Size(497, 39);
            this.lblBannerText.TabIndex = 27;
            this.lblBannerText.Text = "ZC Smart Card Developer Demo";
            // 
            // lblReaderLabel
            // 
            this.lblReaderLabel.AutoSize = true;
            this.lblReaderLabel.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblReaderLabel.ForeColor = System.Drawing.Color.Black;
            this.lblReaderLabel.Location = new System.Drawing.Point(10, 20);
            this.lblReaderLabel.Name = "lblReaderLabel";
            this.lblReaderLabel.Size = new System.Drawing.Size(84, 24);
            this.lblReaderLabel.TabIndex = 13;
            this.lblReaderLabel.Text = "Reader:";
            // 
            // lblBanner
            // 
            this.lblBanner.BackColor = System.Drawing.Color.Black;
            this.lblBanner.Location = new System.Drawing.Point(0, 0);
            this.lblBanner.Name = "lblBanner";
            this.lblBanner.Size = new System.Drawing.Size(1051, 94);
            this.lblBanner.TabIndex = 30;
            this.lblBanner.Text = "label1";
            // 
            // gbButtons
            // 
            this.gbButtons.Controls.Add(this.btnExit);
            this.gbButtons.Controls.Add(this.btnTest);
            this.gbButtons.Controls.Add(this.lblMsg);
            this.gbButtons.Location = new System.Drawing.Point(20, 250);
            this.gbButtons.Name = "gbButtons";
            this.gbButtons.Size = new System.Drawing.Size(1016, 60);
            this.gbButtons.TabIndex = 4;
            this.gbButtons.TabStop = false;
            // 
            // lblReader
            // 
            this.lblReader.AutoSize = true;
            this.lblReader.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblReader.ForeColor = System.Drawing.Color.Black;
            this.lblReader.Location = new System.Drawing.Point(130, 20);
            this.lblReader.Name = "lblReader";
            this.lblReader.Size = new System.Drawing.Size(186, 24);
            this.lblReader.TabIndex = 37;
            this.lblReader.Text = "Readers not found";
            // 
            // lblVersion
            // 
            this.lblVersion.Font = new System.Drawing.Font("Arial", 10F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblVersion.ForeColor = System.Drawing.Color.DimGray;
            this.lblVersion.Location = new System.Drawing.Point(589, 100);
            this.lblVersion.Name = "lblVersion";
            this.lblVersion.Size = new System.Drawing.Size(410, 24);
            this.lblVersion.TabIndex = 39;
            this.lblVersion.Text = "Developer Demo 1.1  Library ";
            this.lblVersion.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            // 
            // gbReaderCard
            // 
            this.gbReaderCard.Controls.Add(this.nbOffset);
            this.gbReaderCard.Controls.Add(this.lblOffset);
            this.gbReaderCard.Controls.Add(this.lblReaderVersion);
            this.gbReaderCard.Controls.Add(this.lblReaderLabel);
            this.gbReaderCard.Controls.Add(this.lblVersion);
            this.gbReaderCard.Controls.Add(this.lblReader);
            this.gbReaderCard.Controls.Add(this.lblCardType);
            this.gbReaderCard.Controls.Add(this.cboCardGroup);
            this.gbReaderCard.Controls.Add(this.cbTestPrint);
            this.gbReaderCard.Controls.Add(this.cboCardType);
            this.gbReaderCard.Controls.Add(this.lblTestPrint);
            this.gbReaderCard.Controls.Add(this.lblTestTypeBasic);
            this.gbReaderCard.Location = new System.Drawing.Point(20, 110);
            this.gbReaderCard.Name = "gbReaderCard";
            this.gbReaderCard.Size = new System.Drawing.Size(1016, 140);
            this.gbReaderCard.TabIndex = 0;
            this.gbReaderCard.TabStop = false;
            // 
            // nbOffset
            // 
            this.nbOffset.Font = new System.Drawing.Font("Arial", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.nbOffset.Location = new System.Drawing.Point(530, 100);
            this.nbOffset.Maximum = new decimal(new int[] {
            300,
            0,
            0,
            0});
            this.nbOffset.Minimum = new decimal(new int[] {
            155,
            0,
            0,
            -2147483648});
            this.nbOffset.Name = "nbOffset";
            this.nbOffset.Size = new System.Drawing.Size(80, 25);
            this.nbOffset.TabIndex = 3;
            // 
            // lblOffset
            // 
            this.lblOffset.AutoSize = true;
            this.lblOffset.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblOffset.ForeColor = System.Drawing.Color.Black;
            this.lblOffset.Location = new System.Drawing.Point(450, 100);
            this.lblOffset.Name = "lblOffset";
            this.lblOffset.Size = new System.Drawing.Size(76, 24);
            this.lblOffset.TabIndex = 43;
            this.lblOffset.Text = "Offset:";
            // 
            // lblReaderVersion
            // 
            this.lblReaderVersion.Font = new System.Drawing.Font("Arial", 10F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblReaderVersion.ForeColor = System.Drawing.Color.Gray;
            this.lblReaderVersion.Location = new System.Drawing.Point(522, 20);
            this.lblReaderVersion.Name = "lblReaderVersion";
            this.lblReaderVersion.Size = new System.Drawing.Size(477, 24);
            this.lblReaderVersion.TabIndex = 41;
            this.lblReaderVersion.Text = "Reader Version";
            this.lblReaderVersion.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            // 
            // pbBannerLogo
            // 
            this.pbBannerLogo.BackColor = System.Drawing.Color.Black;
            this.pbBannerLogo.BackgroundImageLayout = System.Windows.Forms.ImageLayout.None;
            this.pbBannerLogo.Cursor = System.Windows.Forms.Cursors.AppStarting;
            this.pbBannerLogo.Enabled = false;
            this.pbBannerLogo.Image = global::ZCSCDevDemo.Properties.Resources.ZebraLogo1;
            this.pbBannerLogo.Location = new System.Drawing.Point(835, 14);
            this.pbBannerLogo.Margin = new System.Windows.Forms.Padding(0);
            this.pbBannerLogo.Name = "pbBannerLogo";
            this.pbBannerLogo.Size = new System.Drawing.Size(199, 76);
            this.pbBannerLogo.SizeMode = System.Windows.Forms.PictureBoxSizeMode.AutoSize;
            this.pbBannerLogo.TabIndex = 38;
            this.pbBannerLogo.TabStop = false;
            // 
            // FormMain
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.Color.Silver;
            this.ClientSize = new System.Drawing.Size(1050, 318);
            this.Controls.Add(this.gbReaderCard);
            this.Controls.Add(this.pbBannerLogo);
            this.Controls.Add(this.gbButtons);
            this.Controls.Add(this.lblBannerText);
            this.Controls.Add(this.lblBanner);
            this.KeyPreview = true;
            this.Name = "FormMain";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Load += new System.EventHandler(this.FormMain_Load);
            this.gbButtons.ResumeLayout(false);
            this.gbReaderCard.ResumeLayout(false);
            this.gbReaderCard.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.nbOffset)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.pbBannerLogo)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion
        private System.Windows.Forms.ComboBox cboCardType;
        private System.Windows.Forms.Label lblTestTypeBasic;
        private System.Windows.Forms.Button btnExit;
        private System.Windows.Forms.Button btnTest;
        private System.Windows.Forms.Label lblMsg;
        private System.Windows.Forms.CheckBox cbTestPrint;
        private System.Windows.Forms.ComboBox cboCardGroup;
        private System.Windows.Forms.Label lblTestPrint;
        private System.Windows.Forms.Label lblCardType;
        private System.Windows.Forms.Label lblBannerText;
        private System.Windows.Forms.Label lblReaderLabel;
        private System.Windows.Forms.Label lblBanner;
        private System.Windows.Forms.GroupBox gbButtons;
        private System.Windows.Forms.Label lblReader;
        private System.Windows.Forms.PictureBox pbBannerLogo;
        private System.Windows.Forms.Label lblVersion;
        private System.Windows.Forms.GroupBox gbReaderCard;
        private System.Windows.Forms.Label lblReaderVersion;
        private System.Windows.Forms.NumericUpDown nbOffset;
        private System.Windows.Forms.Label lblOffset;
    }
}

