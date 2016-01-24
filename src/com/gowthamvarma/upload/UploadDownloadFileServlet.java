package com.gowthamvarma.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

@WebServlet("/download")
public class UploadDownloadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private ServletFileUpload uploader = null;
	
    @Override
	public void init() throws ServletException{
		DiskFileItemFactory fileFactory = new DiskFileItemFactory();
		File filesDir = (File) getServletContext().getAttribute("FILES_DIR_FILE");
		fileFactory.setRepository(filesDir);
		this.uploader = new ServletFileUpload(fileFactory);
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String fileName = request.getParameter("fileName");
		if(fileName == null || fileName.equals("")){
			doPost(request,response);
		}
		File file = new File(request.getServletContext().getAttribute("FILES_DIR")+File.separator+fileName);
		if(file.exists()){
			System.out.println("File location on server::"+file.getAbsolutePath());
			String type = request.getParameter("type");
			if(type != null && "delete".equals(type)){
				file.delete();
				doPost(request,response);
			}
			ServletContext ctx = getServletContext();
			InputStream fis = null;
			try {
				fis = new FileInputStream(file);
			} catch (Exception e) {
				// do nothing
			} 
			if(fis != null){
				String mimeType = ctx.getMimeType(file.getAbsolutePath());
				response.setContentType(mimeType != null? mimeType:"application/octet-stream");
				response.setContentLength((int) file.length());
				response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
				ServletOutputStream os = response.getOutputStream();
				byte[] bufferData = new byte[1024];
				int read=0;
				while((read = fis.read(bufferData))!= -1){
					os.write(bufferData, 0, read);
				}
				os.flush();
				os.close();
				fis.close();
			}
			System.out.println("File downloaded at client successfully");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!ServletFileUpload.isMultipartContent(request)) {
			// throw new ServletException("Content type is not multipart/form-data");
		}

		try {
			List<FileItem> fileItemsList = uploader.parseRequest(request);
			Iterator<FileItem> fileItemsIterator = fileItemsList.iterator();
			while (fileItemsIterator.hasNext()) {
				FileItem fileItem = fileItemsIterator.next();
				System.out.println("FieldName=" + fileItem.getFieldName());
				System.out.println("FileName=" + fileItem.getName());
				System.out.println("ContentType=" + fileItem.getContentType());
				System.out.println("Size in bytes=" + fileItem.getSize());
				File file = new File(request.getServletContext().getAttribute("FILES_DIR") + File.separator + fileItem.getName());
				System.out.println("Absolute Path at server=" + file.getAbsolutePath());
				try {
					fileItem.write(file);
				} catch (Exception e) {
					// do nothing if no files is chosen in UI page
				}
			}
			response.sendRedirect(response.encodeRedirectURL("."));
		} catch (Exception e) {
			System.out.println("download only request");
		} 
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.write("<html><head>");
		out.write("<link rel=\"stylesheet\" href=\"table.css\" type=\"text//css\"/>");
		out.write("</head><body>");
		try {
			File folder = new File(request.getServletContext().getAttribute("FILES_DIR") + File.separator);
			File[] listOfFiles = folder.listFiles();
			out.write("<br>");
			
			if(listOfFiles.length == 0) {
				out.write("<p>No files to download.</p>");
			} else {
				out.write("<div class=\"CSSTableGenerator\" style=\"width:600px;height:150px;\">");
				out.write("<table cellpadding=\"10\">");
				out.write("<tr><td>Name</td><td>Size</td><td>Download</td><td>Delete</td></tr>");
				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile()) {
						String fileName = listOfFiles[i].getName();
						System.out.println("File " + fileName);
						File file = new File(request.getServletContext().getAttribute("FILES_DIR") + File.separator + fileName);
						System.out.println("Absolute Path at server=" + file.getAbsolutePath());
						out.write("<tr>");
						
						out.write("<td>");
						out.write(fileName);
						out.write("</td>");
						
						DecimalFormat df = new DecimalFormat("#.##");
						String size = "";
						int const_1024 = 1024;
						double bytes = file.length();
						double kilobytes = (bytes / const_1024);
						if(kilobytes > const_1024){
							double megabytes = (kilobytes / 1024);
							if(megabytes > const_1024){
								double gigabytes  = (megabytes / 1024);
								size = df.format(gigabytes) + " GB";
							} else {
								size = df.format(megabytes) + " MB";
							}
						} else {
							size = df.format(kilobytes) + " KB";
						}
						
						
						out.write("<td>");
						out.write(size);
						out.write("</td>");
						
						out.write("<td>");
						out.write("<a href=\"download?fileName=" + fileName + "\"><img src=\"images\\download.png\" alt=\"HTML tutorial\" style=\"width:22px;height:22px;border:0\"></a>");
						out.write("</td>");
						
						out.write("<td>");
						out.write("<a href=\"download?type=delete&fileName=" + fileName + "\"><img src=\"images\\delete.png\" alt=\"HTML tutorial\" style=\"width:22px;height:22px;border:0\"></a>");
						out.write("</td>");
						
						out.write("</tr>");
					} else if (listOfFiles[i].isDirectory()) {
						System.out.println("Directory " +  listOfFiles[i].getName());
					}
				}
				out.write("</table>");
				out.write("</div>");
			}
		} catch (Exception e) {
			out.write("Exception in uploading file.");
		}
		out.write("</body></html>");
	}

}
