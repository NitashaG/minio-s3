package org.vmware.s3.pdf.custom;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.Iterator;

import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;
import org.springframework.integration.util.CloseableIterator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.util.StringUtils;

import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.ReaderProperties;

public class PDFSplitter extends AbstractMessageSplitter {
	@Override
	protected Object splitMessage(Message<?> message) {
		System.out.println("****Custom PDFSplitter>> SplitMessage****");
		Object payload = message.getPayload();
		

		Reader reader;

		String filePath;
		System.out.println(">>>payloadType>>"+ payload.getClass());
		if (payload instanceof String) {
			try {
				reader = new FileReader((String) payload);
				filePath = (String) payload;
			}
			catch (FileNotFoundException e) {
				throw new MessageHandlingException(message, "failed to read file [" + payload + "]", e);
			}
		}
		else if (payload instanceof File) {
			try {
				reader = new FileReader((File) payload);
				filePath = ((File) payload).getAbsolutePath();
			}
			catch (FileNotFoundException e) {
				throw new MessageHandlingException(message, "failed to read file [" + payload + "]", e);
			}
		}
		else if (payload instanceof InputStream) {
			reader = new InputStreamReader((InputStream) payload);
			filePath = buildPathFromMessage(message, ":stream:");
		}
		else if (payload instanceof Reader) {
			reader = (Reader) payload;
			filePath = buildPathFromMessage(message, ":reader:");
		}
		else {
			return message;
		}
		System.out.println(">>>>>>>>>FilePath >>"+ filePath);
		Iterator<Object> iterator = messageToFileIterator(filePath);
		return iterator;
	}
	private String buildPathFromMessage(Message<?> message, String defaultPath) {
		String remoteDir = (String) message.getHeaders().get(FileHeaders.REMOTE_DIRECTORY);
		String remoteFile = (String) message.getHeaders().get(FileHeaders.REMOTE_FILE);
		if (StringUtils.hasText(remoteDir) && StringUtils.hasText(remoteFile)) {
			return remoteDir + remoteFile;
		}
		else {
			return defaultPath;
		}
	}
	private Iterator<Object> messageToFileIterator(String filePath) {
//		BufferedReader bufferedReader = wrapToBufferedReader(message, reader);
		return new PDFIterator(filePath);
	}

//	private BufferedReader wrapToBufferedReader(Message<?> message, Reader reader) {
//		return new BufferedReader(reader) {
//
//			@Override
//			public void close() throws IOException {
//				try {
//					super.close();
//				}
//				finally {
//					Closeable closeableResource = StaticMessageHeaderAccessor.getCloseableResource(message);
//					if (closeableResource != null) {
//						closeableResource.close();
//					}
//				}
//			}
//
//		};
//	}
	private class PDFIterator implements CloseableIterator<Object> {

			private final String filePath;

			private int pageCount = 0;
			private PdfReader reader;
			private PdfDocument sourceDoc;
			private int currentPage = 0;
			private String outputFilePrefix;
			private Date dt;
			

			PDFIterator( 
					String filePath) {
				this.dt = new Date();
				this.filePath = filePath;
				this.outputFilePrefix = filePath.substring(filePath.lastIndexOf("/")+1,filePath.length());
				System.out.println("**outputFilePrefix**"+outputFilePrefix);
				try{
					this.reader = new PdfReader(new RandomAccessSourceFactory().createBestSource(this.filePath),new ReaderProperties());
					reader.setMemorySavingMode(true);
					System.out.println("Constructing Source doc now");
					this.sourceDoc = new PdfDocument(this.reader);
					this.pageCount = this.sourceDoc.getNumberOfPages();
				}
				catch(IOException e) {
					
				}
			}

			@Override
			public boolean hasNext() {
				return (pageCount!=0 && currentPage<pageCount);
			}
			

			@Override
			public Object next() {
				currentPage++;
				Date dt = new Date();
				ByteArrayOutputStream out = new ByteArrayOutputStream(); 
				PdfWriter writer = new PdfWriter(out);
				PdfDocument outDoc = new PdfDocument(writer);
				this.sourceDoc.copyPagesTo(currentPage, currentPage, outDoc);
				outDoc.close();
				AbstractIntegrationMessageBuilder<byte[]> messageBuilder =
						getMessageBuilderFactory()
								.withPayload(out.toByteArray())
								.setHeader("file_name", outputFilePrefix.replace(".pdf","-"+ currentPage+"-"+this.dt.getTime()+".pdf"));
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return messageBuilder;
			}

			@Override
			public void close() {
				
				this.sourceDoc.close();
				try {
					this.reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			

		}

}
