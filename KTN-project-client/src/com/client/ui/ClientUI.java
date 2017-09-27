package com.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.client.core.Client;

public class ClientUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8938540262220887637L;
	
	private ClientUI ui;
	
	public ClientUI() {
		ui = this;
		init();
	}
	
	private ArrayList<String> lines = new ArrayList<String>();
	
	private Client client;
	private DefaultListModel<String> userListModel;
	private JTextPane chatArea;
	private JScrollPane chatScroll;
	private JButton connectButton, sendMessageButton;
	private JTextField portField, ipField, messageField;
	private String username;
	
	private void init() {
		setSize(1000, 600);
		setTitle("Chat");
		setLayout(null);
		setResizable(false);
		
	    try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		
		ipField = new JTextField("localhost");
		ipField.setBounds(0, 0, 175, 40);
		add(ipField);
		
		portField = new JTextField("5000");
		portField.setBounds(175, 0, 175, 40);
		add(portField);
		
		connectButton = new JButton("Connect to server");
		connectButton.setBounds(350, 0, 645, 40);
		add(connectButton);
		
		connectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (connectButton.getText().equals("Disconnect from server")) {
					client.quit();
					pushMessage("Disconnected from server.", true);
				} else {
					try {
						String ip = ipField.getText();
						int port = Integer.parseInt(portField.getText());
						username = JOptionPane.showInputDialog("Username: ");
						String password = JOptionPane.showInputDialog("Password: ");
						if (client != null)
							client.quit();
						if (username.length() > 0 && validateUsername(username)) {
							lines.clear();
							pushMessage("Connecting to server with username: "+username, true);
							setClient(new Client(ui, username, password, ip, port));
							if (client.connect(client.getIP(), client.getPort())) {
								connectButton.setText("Disconnect from server");
								setTitle("Chat [Connected]");
							}
						} else {
							JOptionPane.showMessageDialog(null, "Invalid username, the username can only contain characters and numbers");
						}
					} catch (NumberFormatException e) {
						
					}
				}
			}
			
		});
		
		userListModel = new DefaultListModel<String>();
		JList<String> userList = new JList<String>(userListModel);
		final JScrollPane userScroll = new JScrollPane(userList);
		userScroll.setBounds(0, 40, 350, 480);
		add(userScroll);
		
		userList.addMouseListener(new MouseAdapter() {
	        public void mousePressed(MouseEvent e) {
	            if (e.getClickCount() == 2) {
	            	if (!userListModel.getElementAt(userList.getSelectedIndex()).replace(" (you)", "").equals(username)) {
		            	String message = JOptionPane.showInputDialog("Send a PM to the user: " + userListModel.getElementAt(userList.getSelectedIndex()));
		            	client.sendMessage("request", "pm", "content", message, "user", userListModel.getElementAt(userList.getSelectedIndex()));
		    			pushMessage("    [PM to " + userListModel.getElementAt(userList.getSelectedIndex()) + "]: " + message);
	            	}
	            }
	        }
	    });
		
		chatArea = new JTextPane();
		chatArea.setContentType("text/html");
		chatArea.setEditable(false);
		chatScroll = new JScrollPane(chatArea);
		chatScroll.setBounds(350, 40, 643, 480);
		add(chatScroll);
		
		messageField = new JTextField("Type message here");
		messageField.setBounds(0, 520, 750, 40);
		add(messageField);
		
		messageField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					sendMessageButton.doClick();
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}
			
		});
		
		sendMessageButton = new JButton("Send message");
		sendMessageButton.setBounds(750, 520, 245, 40);
		add(sendMessageButton);
		
		sendMessageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (client != null) {
					if (!messageField.getText().equals("")) {
						if (messageField.getText().startsWith("kick")) {
							client.sendMessage("request", "kick", "content", messageField.getText().split(" ")[1]);
						} else if (messageField.getText().equals("help")) {
							client.sendMessage("request", "help", "content", "None");
						} else {
							client.sendMessage("request", "msg", "content", messageField.getText());
							pushMessage("[" + client.getTimestamp()+", " + username + "]: " + messageField.getText());
						}
					}
				}
				messageField.setText("");
			}
			
		});
		
	    addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
            	if (client != null)
	            	client.quit();
	            e.getWindow().dispose();
            }
        });
		
		setVisible(true);
	}
	
	public void updateUsers() {
		if (client != null && client.getUsers() != null) {
			userListModel.clear();
			for (String user : client.getUsers()) {
				userListModel.addElement(user.equals(username) ? user+" (you)" : user);
			}
		}
	}
	
	public void setClient(Client c) {
		this.client = c;
		if (c == null) {
			connectButton.setText("Connect to server");
			setTitle("Chat");
		}
	}
	
	public static void main(String...args) {
		new ClientUI();
	}
	
	public void pushMessage(String message) {
		pushMessage(message, false);
	}
	
	public void pushMessage(String message, boolean bold) {
		lines.add("<a>" + (bold ? "<b>" : "") + insertCommands(message) + (bold ? "</b>" : "") + "</a><br>");
		chatArea.setText(getHtml());
		chatScroll.getVerticalScrollBar().setValue(chatScroll.getVerticalScrollBar().getMaximum());
	}

	private boolean validateUsername(String name) {
		return name.matches("^[a-zA-Z0-9]*$");
	}
	
	private String getHtml() {
		String html = "<!DOCTYPE HTML><html><body><font size=\"5\">";
		for (String line : lines) {
			html += line;
		}
		html += "</font></body></html>";
		return html;
	}
	
	private String[][] customCommands = {{":)", "<img src=\"file:src/com/res/smile.png\"/>"},
			{":(", "<img src=\"file:src/com/res/nosmile.png\" width=\"16\" height=\"16\"/>"},
			{":troll:", "<img src=\"file:src/com/res/trollface.png\"/>"},
			{"8)", "<img src=\"file:src/com/res/coolface.png\"/>"}, 
			{":P", "<img src=\"file:src/com/res/tongueface.png\"/>"}};
	
	private String insertCommands(String in) {
		String newLine = in;
		for (String[] commands : customCommands) {
			newLine = newLine.replace(commands[0], commands[1]);
		}
		return newLine;
	}
	
}
