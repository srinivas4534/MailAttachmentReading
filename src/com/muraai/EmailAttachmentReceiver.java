package com.muraai;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

public class EmailAttachmentReceiver {
	private String saveDirectory;


	public void setSaveDirectory(String dir) {
		this.saveDirectory = dir;
	}


	public void downloadEmailAttachments(String host, String port,
			String userName, String password) {
		Properties properties = new Properties();


		properties.put("mail.pop3.host", host);
		properties.put("mail.pop3.port", port);

		
		properties.setProperty("mail.pop3.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		properties.setProperty("mail.pop3.socketFactory.fallback", "false");
		properties.setProperty("mail.pop3.socketFactory.port",String.valueOf(port));

		Session session = Session.getDefaultInstance(properties);

		try {
			
			Store store = session.getStore("pop3");
			store.connect(userName, password);

			
			Folder folderInbox = store.getFolder("INBOX");
			folderInbox.open(Folder.READ_ONLY);

		
			Message[] arrayMessages = folderInbox.getMessages(10,15);

			for (int i = 0; i < arrayMessages.length; i++) {
				Message message = arrayMessages[i];
				Address[] fromAddress = message.getFrom();
				String from = fromAddress[0].toString();
				String subject = message.getSubject();
				String sentDate = message.getSentDate().toString();

				String contentType = message.getContentType();
				System.out.println("Mime type = "+contentType);
				String messageContent = "";

				
				String attachFiles = "";

				if (message.isMimeType("multipart/*")) {
					
					System.out.println(message.isMimeType("multipart/*"));
					Multipart multiPart = (Multipart) message.getContent();
					int numberOfParts = multiPart.getCount();
					
					for (int partCount = 0; partCount < numberOfParts; partCount++)
					{
						MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
						if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
							// attachment
							String fileName = part.getFileName();
							attachFiles += fileName + ", ";
							part.saveFile(saveDirectory + File.separator + fileName);
						} else {
							messageContent = part.getContent().toString();
						}
					}

					if (attachFiles.length() > 1) {
						attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
					}
				} 
				else if (contentType.contains("text/plain")	|| contentType.contains("text/html"))
				{
					Object content = message.getContent();
					if (content != null) {
						messageContent = content.toString();
					}
				}

				
				System.out.println("Message #" + (i + 1) + ":");
				System.out.println("\t From: " + from);
				System.out.println("\t Subject: " + subject);
				System.out.println("\t Sent Date: " + sentDate);
				System.out.println("\t Message: " + messageContent);
				System.out.println("\t Attachments: " + attachFiles);
			}

			
			folderInbox.close(false);
			store.close();
		} catch (NoSuchProviderException ex) {
			System.out.println("No provider for pop3.");
			ex.printStackTrace();
		} catch (MessagingException ex) {
			System.out.println("Could not connect to the message store");
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		String host = "pop.gmail.com";
		String port = "995";
		String userName = "trainee.batch2016@muraai.com";
		String password = "Muraai@1234";

		String saveDirectory = "D:/h4";

		EmailAttachmentReceiver receiver = new EmailAttachmentReceiver();
		receiver.setSaveDirectory(saveDirectory);
		receiver.downloadEmailAttachments(host, port, userName, password);

	}
}
