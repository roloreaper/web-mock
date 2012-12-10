package org.httpmock.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.*;

/**
 * A simple, tiny, nicely embeddable HTTP 1.0 server in Java
 * <p/>
 * <p> NanoHTTPD version 1.21,
 * Copyright &copy; 2001,2005-2011 Jarno Elonen (elonen@iki.fi, http://iki.fi/elonen/)
 * and Copyright &copy; 2010 Konstantinos Togias (info@ktogias.gr, http://ktogias.gr)
 * <p/>
 * <p><b>Features + limitations: </b><ul>
 * <p/>
 * <li> Only one Java file </li>
 * <li> Java 1.1 compatible </li>
 * <li> Released as open source, Modified BSD licence </li>
 * <li> No fixed config files, logging, authorization etc. (Implement yourself if you need them.) </li>
 * <li> Supports parameter parsing of GET and POST methods </li>
 * <li> Supports both dynamic content and file serving </li>
 * <li> Supports file upload (since version 1.2, 2010) </li>
 * <li> Never caches anything </li>
 * <li> Doesn't limit bandwidth, request time or simultaneous connections </li>
 * <li> Default code serves files and shows all HTTP parameters and headers</li>
 * <li> File server supports directory listing, index.html and index.htm </li>
 * <li> File server does the 301 redirection trick for directories without '/'</li>
 * <li> File server supports simple skipping for files (continue download) </li>
 * <li> File server uses current directory as a web root </li>
 * <li> File server serves also very long files without memory overhead </li>
 * <li> Contains a built-in list of most common mime types </li>
 * <li> All header names are converted lowercase so they don't vary between browsers/clients </li>
 * <p/>
 * </ul>
 * <p/>
 * <p><b>Ways to use: </b><ul>
 * <p/>
 * <li> Run as a standalone app, serves files from current directory and shows requests</li>
 * <li> Subclass serve() and embed to your own program </li>
 * <li> Call serveFile() from serve() with your own base directory </li>
 * <p/>
 * </ul>
 * <p/>
 * See the end of the source file for distribution license
 * (Modified BSD licence)
 */
abstract class NanoHTTPD {
    public static final String HEADER = "header";
    public static final String PRE = "pre";
    public static final String PARAMS = "params";
    public static final int KB_8 = 8192;
    public static final long DEFAULT_SIZE = 0x7FFFFFFFFFFFFFFFl;
    public static final int READ_BODY_SIZE = 512;
    public static final int TWO = 2;
    public static final int FOUR = 4;
    public static final int KB_2 = 2048;
    // ==================================================
	// API parts
	// ==================================================

	/**
	 * Override this to customize the server.<p>
	 * <p/>
	 * (By default, this delegates to serveFile() and allows directory listing.)
	 *
	 * @param uri	Percent-decoded URI without parameters, for example "/index.cgi"
	 * @param method "GET", "POST" etc.
	 * @param parms  Parsed, percent decoded parameters from URI and, in case of POST, data.
	 * @param header Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 */
	protected abstract Response serve(String uri, String method, Properties header, Properties parms, Properties files);

	// ==================================================
	// Socket & server code
	// ==================================================

	/**
	 * Starts a HTTP server to given port.<p>
	 * Throws an IOException if the socket is already in use
	 */
	NanoHTTPD(int port) throws IOException {
		myTcpPort = port;
		myServerSocket = new ServerSocket(myTcpPort);
		myThread = new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						new HTTPSession(myServerSocket.accept());
                    }
				} catch (IOException ioe) {
				}
			}
		});
		myThread.setDaemon(true);
		myThread.start();
	}

	/**
	 * Stops the server.
	 */
	public void stop() {
		try {
			myServerSocket.close();
			myThread.join();
		} catch (IOException ioe) {
		} catch (InterruptedException e) {
		}
	}


	/**
	 * Handles one session, i.e. parses the HTTP request
	 * and returns the response.
	 */
	private class HTTPSession implements Runnable {
		public HTTPSession(Socket s) {
			mySocket = s;
			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
		}

		public void run() {
			try {
				InputStream is = mySocket.getInputStream();
				if (is == null)  {return; }

				// Read the first 8192 bytes.
				// The full header should fit in here.
				// Apache's default header limit is 8KB.
				int bufsize = KB_8;
				byte[] buf = new byte[bufsize];
				int rlen = is.read(buf, 0, bufsize);
				if (rlen <= 0) {return;}

				// Create a BufferedReader for parsing the header.
				ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
				BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));
				//Properties pre = new Properties();
				//Properties parms = new Properties();
				//Properties header = new Properties();
				Properties files = new Properties();


				// Decode the header into parms and header java properties
				Map<String,Properties> headers =decodeHeader(hin);
				String method = getPre(headers).getProperty("method");
				String uri = getPre(headers).getProperty("uri");

				long size = DEFAULT_SIZE;
				String contentLength = getHeader(headers).getProperty("content-length");
				if (contentLength != null) {
					try {
						size = Integer.parseInt(contentLength);
					} catch (NumberFormatException ex) {
					}
				}

				// We are looking for the byte separating header from body.
				// It must be the last byte of the first two sequential new lines.
				int splitbyte = 0;
				boolean sbfound = false;
				while (splitbyte < rlen) {
					if (buf[splitbyte] == '\r' && buf[++splitbyte] == '\n' && buf[++splitbyte] == '\r' && buf[++splitbyte] == '\n') {
						sbfound = true;
						break;
					}
					splitbyte++;
				}
				splitbyte++;

				// Write the part of body already read to ByteArrayOutputStream f
				ByteArrayOutputStream f = new ByteArrayOutputStream();
				if (splitbyte < rlen) {f.write(buf, splitbyte, rlen - splitbyte);}

				// While Firefox sends on the first read all the data fitting
				// our buffer, Chrome and Opera sends only the headers even if
				// there is data for the body. So we do some magic here to find
				// out whether we have already consumed part of body, if we
				// have reached the end of the data to be sent or we should
				// expect the first byte of the body at the next read.
				if (splitbyte < rlen) {
					size -= rlen - splitbyte + 1;
                }
				else if (!sbfound || size == DEFAULT_SIZE) {
					size = 0;
                }

				// Now read all the body and write it to f
				buf = new byte[READ_BODY_SIZE];
				while (rlen >= 0 && size > 0) {
					rlen = is.read(buf, 0, READ_BODY_SIZE);
					size -= rlen;
					if (rlen > 0) {
						f.write(buf, 0, rlen);
                    }
				}

				// Get the raw body as a byte []
				byte[] fbuf = f.toByteArray();

				// Create a BufferedReader for easily reading it as string.
				ByteArrayInputStream bin = new ByteArrayInputStream(fbuf);
				BufferedReader in = new BufferedReader(new InputStreamReader(bin));

				// If the method is POST, there may be parameters
				// in data section, too, read it:
                handelPost(files, headers, method, fbuf, in);

                // Ok, now do the serve()
                serveResponse(files, headers, method, uri);

				in.close();
				is.close();
			} catch (IOException ioe) {
				try {
					sendError(HTTPStatusCode.HTTP_INTERNALERROR.toString(), "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
				} catch (Exception ignored) {
				}
			} catch (InterruptedException ignored) {
			}
		}

        private void handelPost(Properties files, Map<String, Properties> headers, String method, byte[] fbuf, BufferedReader in) throws IOException, InterruptedException {
            if (method.equalsIgnoreCase("POST")) {
                String contentType = "";
                String contentTypeHeader = getHeader(headers).getProperty("content-type");
                if (contentTypeHeader == null) {
                    handelurlEncodeParametes(headers, in);
                } else {
                    StringTokenizer st = new StringTokenizer(contentTypeHeader, "; ");
                    if (st.hasMoreTokens()) {
                        contentType = st.nextToken();
                    }

                    if (contentType.equalsIgnoreCase("multipart/form-data")) {
                        // Handle multipart/form-data
                        handleMultiPartFormData(files, headers, fbuf, in, st);
                    } else {
                        // Handle application/x-www-form-urlencoded
                        handelurlEncodeParametes(headers, in);
                    }
                }
            }
        }

        private void handleMultiPartFormData(Properties files, Map<String, Properties> headers, byte[] fbuf, BufferedReader in, StringTokenizer st) throws InterruptedException {
            if (!st.hasMoreTokens()) {
                sendError(HTTPStatusCode.HTTP_BADREQUEST.toString(), "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
            }
            String boundaryExp = st.nextToken();
            StringTokenizer secondPart = new StringTokenizer(boundaryExp, "=");
            if (secondPart.countTokens() != TWO) {
                sendError(HTTPStatusCode.HTTP_BADREQUEST.toString(), "BAD REQUEST: Content type is multipart/form-data but boundary syntax error. Usage: GET /example/file.html");
            }
            secondPart.nextToken();
            String boundary = secondPart.nextToken();

            decodeMultipartData(boundary, fbuf, in, getParams(headers), files);
        }

        private void handelurlEncodeParametes(Map<String, Properties> headers, BufferedReader in) throws IOException, InterruptedException {
            String postLine = "";
            char pbuf[] = new char[READ_BODY_SIZE];
            int read = in.read(pbuf);
            while (read >= 0 && !postLine.endsWith("\r\n")) {
                postLine += String.valueOf(pbuf, 0, read);
                read = in.read(pbuf);
            }
            postLine = postLine.trim();
            getParams(headers).putAll(decodeParms(postLine));
        }

        private void serveResponse(Properties files, Map<String, Properties> headers, String method, String uri) throws InterruptedException {
            Response r = serve(uri, method, getHeader(headers), getParams(headers), files);
            if (r == null) {
                sendError(HTTPStatusCode.HTTP_INTERNALERROR.toString(), "SERVER INTERNAL ERROR: Serve() returned a null response.");
            }
            else {
                sendResponse(r.getStatus(), r.getMimeType(), r.getHeader(), r.getData());
            }
        }

        private Properties getHeader(Map<String, Properties> headers) {
            return getOrCreateIfNessasary(headers,HEADER);
        }

        private Properties getPre(Map<String, Properties> propertiesMap) {
            return getOrCreateIfNessasary(propertiesMap,PRE);
        }

		/**
		 * Decodes the sent headers and loads the data into
		 * java Properties' key - value pairs
		 */
		private Map<String, Properties> decodeHeader(BufferedReader in)
				throws InterruptedException {
            Map<String, Properties> propertiesMap= new HashMap<String, Properties>();
            try {
				// Read the request line
				String inLine = in.readLine();
				if (inLine == null) {return propertiesMap;}
				StringTokenizer st = new StringTokenizer(inLine);
				if (!st.hasMoreTokens()) {
					sendError(HTTPStatusCode.HTTP_BADREQUEST.toString(), "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
                }
				String method = st.nextToken();
				getPre(propertiesMap).put("method", method);

				if (!st.hasMoreTokens()) {
					sendError(HTTPStatusCode.HTTP_BADREQUEST.toString(), "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
                }

				String uri = st.nextToken();

				// Decode parameters from the URI
                uri = decodeParametersFromURI(propertiesMap, uri);
                // If there's another token, it's protocol version,
                // followed by HTTP headers. Ignore version but parse headers.
                // NOTE: this now forces header names lowercase since they are
                // case insensitive and vary by client.
                decodeProtocolVersionIfNessasary(in, propertiesMap, st);
                getPre(propertiesMap).put("uri", uri);
			} catch (IOException ioe) {
				sendError(HTTPStatusCode.HTTP_INTERNALERROR.toString(), "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			}
            return propertiesMap;
        }

        private void decodeProtocolVersionIfNessasary(BufferedReader in, Map<String, Properties> propertiesMap, StringTokenizer st) throws IOException {

            if (st.hasMoreTokens()) {
                String line = in.readLine();
                while (line != null && line.trim().length() > 0) {
                    int p = line.indexOf(':');
                    if (p >= 0) {
                        getHeader(propertiesMap).put(line.substring(0, p).trim().toLowerCase(), line.substring(p + 1).trim());
                    }
                    line = in.readLine();
                }
            }
        }

        private String decodeParametersFromURI(Map<String, Properties> propertiesMap, String uri) throws InterruptedException {
            int qmi = uri.indexOf('?');
            if (qmi >= 0) {
                getParams(propertiesMap).putAll(decodeParms(uri.substring(qmi + 1)));
                uri = decodePercent(uri.substring(0, qmi));
            } else uri = decodePercent(uri);
            return uri;
        }

        private Properties getParams(Map<String, Properties> propertiesMap) {
            return getOrCreateIfNessasary(propertiesMap,PARAMS);
        }

		/**
		 * Decodes the Multipart Body data and put it
		 * into java Properties' key - value pairs.
		 */
		private void decodeMultipartData(String boundary, byte[] fbuf, BufferedReader in, Properties parms, Properties files)
				throws InterruptedException {
			try {
				int[] bpositions = getBoundaryPositions(fbuf, boundary.getBytes());
				int boundarycount = 1;
				String mpline = in.readLine();
				while (mpline != null) {
					if (mpline.indexOf(boundary) == -1) {
						sendError(HTTPStatusCode.HTTP_BADREQUEST.toString(), "BAD REQUEST: Content type is multipart/form-data but next chunk does not start with boundary. Usage: GET /example/file.html");
                    }
					boundarycount++;
					Properties item = new Properties();
					mpline = in.readLine();
					while (mpline != null && mpline.trim().length() > 0) {
						int p = mpline.indexOf(':');
						if (p != -1) {
							item.put(mpline.substring(0, p).trim().toLowerCase(), mpline.substring(p + 1).trim());
                        }
						mpline = in.readLine();
					}
					if (mpline != null) {
						String contentDisposition = item.getProperty("content-disposition");
						if (contentDisposition == null) {
							sendError(HTTPStatusCode.HTTP_BADREQUEST.toString(), "BAD REQUEST: Content type is multipart/form-data but no content-disposition info found. Usage: GET /example/file.html");
						}
						StringTokenizer st = new StringTokenizer(contentDisposition, "; ");
						Properties disposition = new Properties();
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							int p = token.indexOf('=');
							if (p != -1) {
								disposition.put(token.substring(0, p).trim().toLowerCase(), token.substring(p + 1).trim());
                            }
						}
						String pname = disposition.getProperty("name");
						pname = pname.substring(1, pname.length() - 1);

						String value = "";
						if (item.getProperty("content-type") == null) {
							while (mpline != null && !mpline.contains(boundary)) {
								mpline = in.readLine();
								if (mpline != null) {
									int d = mpline.indexOf(boundary);
									if (d == -1) {
										value += mpline;
                                    }
									else {
										value += mpline.substring(0, d - 2);
                                    }
								}
							}
						} else {
							if (boundarycount > bpositions.length) {
								sendError(HTTPStatusCode.HTTP_INTERNALERROR.toString(), "Error processing request");
                            }
							int offset = stripMultipartHeaders(fbuf, bpositions[boundarycount - 2]);
							String path = saveTmpFile(fbuf, offset, bpositions[boundarycount - 1] - offset - FOUR);
							files.put(pname, path);
							value = disposition.getProperty("filename");
							value = value.substring(1, value.length() - 1);
							do {
								mpline = in.readLine();
							} while (mpline != null && mpline.indexOf(boundary) == -1);
						}
						parms.put(pname, value);
					}
				}
			} catch (IOException ioe) {
				sendError(HTTPStatusCode.HTTP_INTERNALERROR.toString(), "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			}
		}

		/**
		 * Find the byte positions where multipart boundaries start.
		 */
		public int[] getBoundaryPositions(byte[] b, byte[] boundary) {
			int matchcount = 0;
			int matchbyte = -1;
			Vector matchbytes = new Vector();
			for (int i = 0; i < b.length; i++) {
				if (b[i] == boundary[matchcount]) {
					if (matchcount == 0) {
						matchbyte = i;
                    }
					matchcount++;
					if (matchcount == boundary.length) {
						matchbytes.addElement(Integer.valueOf(matchbyte));
						matchcount = 0;
						matchbyte = -1;
					}
				} else {
					i -= matchcount;
					matchcount = 0;
					matchbyte = -1;
				}
			}
			int[] ret = new int[matchbytes.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = ((Integer) matchbytes.elementAt(i)).intValue();
			}
			return ret;
		}

		/**
		 * Retrieves the content of a sent file and saves it
		 * to a temporary file.
		 * The full path to the saved file is returned.
		 */
		private String saveTmpFile(byte[] b, int offset, int len) {
			String path = "";
			if (len > 0) {
				String tmpdir = System.getProperty("java.io.tmpdir");
                OutputStream fileStream = null;
				try {
					File temp = File.createTempFile("NanoHTTPD", "", new File(tmpdir));
                    fileStream = new FileOutputStream(temp);
					fileStream.write(b, offset, len);
					fileStream.close();
					path = temp.getAbsolutePath();
				} catch (Exception e) { // Catch exception if any
					System.err.println("Error: " + e.getMessage());
				}
                finally {
                    if (fileStream!=null) {
                        try {
                            fileStream.close();
                        } catch (IOException ignored) {

                        }
                    }
                }
			}
			return path;
		}


		/**
		 * It returns the offset separating multipart file headers
		 * from the file's data.
		 */
		private int stripMultipartHeaders(byte[] b, int offset) {
			int i = 0;
			for (i = offset; i < b.length; i++) {
				if (b[i] == '\r' && b[++i] == '\n' && b[++i] == '\r' && b[++i] == '\n') {
					break;
                }
			}
			return i + 1;
		}

		/**
		 * Decodes the percent encoding scheme. <br/>
		 * For example: "an+example%20string" -> "an example string"
		 */
		private String decodePercent(String str) throws InterruptedException {
			try {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < str.length(); i++) {
					char c = str.charAt(i);
					switch (c) {
						case '+':
							sb.append(' ');
							break;
						case '%':
							sb.append((char) Integer.parseInt(str.substring(i + 1, i + 3), 16));
							i += 2;
							break;
						default:
							sb.append(c);
							break;
					}
				}
				return sb.toString();
			} catch (Exception e) {
				sendError(HTTPStatusCode.HTTP_BADREQUEST.toString(), "BAD REQUEST: Bad percent-encoding.");
				return null;
			}
		}

		/**
		 * Decodes parameters in percent-encoded URI-format
		 * ( e.g. "name=Jack%20Daniels&pass=Single%20Malt" ) and
		 * adds them to given Properties. NOTE: this doesn't support multiple
		 * identical keys due to the simplicity of Properties -- if you need multiples,
		 * you might want to replace the Properties with a Hashtable of Vectors or such.
		 */
		private Properties decodeParms(String parms)
				throws InterruptedException {
            Properties p = new Properties();

			StringTokenizer st = new StringTokenizer(parms, "&");
			while (st.hasMoreTokens()) {
				String e = st.nextToken();
				int sep = e.indexOf('=');
				if (sep >= 0) {
					p.put(decodePercent(e.substring(0, sep)).trim(),
							decodePercent(e.substring(sep + 1)));
                }
			}

            return p;
		}

		/**
		 * Returns an error message as a HTTP response and
		 * throws InterruptedException to stop further request processing.
		 */
		private void sendError(String status, String msg) throws InterruptedException {
			sendResponse(status, MimeType.MIME_PLAINTEXT.toString(), null, new ByteArrayInputStream(msg.getBytes()));
			throw new InterruptedException();
		}

		/**
		 * Sends given response to the socket.
		 */
		private void sendResponse(String status, String mime, Properties header, InputStream data) {
			try {
				if (status == null) {
					throw new Error("sendResponse(): Status can't be null.");
                }

				OutputStream out = mySocket.getOutputStream();
				PrintWriter pw = new PrintWriter(out);
				pw.print("HTTP/1.0 " + status + " \r\n");

				if (mime != null) {
					pw.print("Content-Type: " + mime + "\r\n");
                }

				if (header == null || header.getProperty("Date") == null) {
					pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
                }

				if (header != null) {
					Enumeration e = header.keys();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						String value = header.getProperty(key);
						pw.print(key + ": " + value + "\r\n");
					}
				}
				pw.print("\r\n");
				pw.flush();
                writeDataIfNessasary(data, out);
                out.flush();
				out.close();
				if (data != null) {
					data.close();
                }
			} catch (IOException ioe) {
				// Couldn't write? No can do.
				try {
					mySocket.close();
				} catch (Exception ignored) {
				}
			}
		}

        private void writeDataIfNessasary(InputStream data, OutputStream out) throws IOException {
            if (data != null) {
                byte[] buff = new byte[KB_2];
                while (true) {
                    int read = data.read(buff, 0, KB_2);
                    if (read <= 0) {
                        break;
}
                    out.write(buff, 0, read);
                }
            }
        }

        private Socket mySocket;
	}

    private Properties getOrCreateIfNessasary(Map<String, Properties> propertiesMap, String key) {
        if (propertiesMap.get(key)==null) {
            propertiesMap.put(key,new Properties());
        }
        return propertiesMap.get(key);
    }

	/**
	 * URL-encodes everything between "/"-characters.
	 * Encodes spaces as '%20' instead of '+'.
	 */
	private String encodeUri(String uri) {
		String newUri = "";
		StringTokenizer st = new StringTokenizer(uri, "/ ", true);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.equals("/"))
				newUri += "/";
			else if (tok.equals(" "))
				newUri += "%20";
			else {
				newUri += URLEncoder.encode(tok);
				// For Java 1.4 you'll want to use this instead:
				// try { newUri += URLEncoder.encode( tok, "UTF-8" ); } catch ( java.io.UnsupportedEncodingException uee ) {}
			}
		}
		return newUri;
	}

	private int myTcpPort;
	private final ServerSocket myServerSocket;
	private Thread myThread;

	// ==================================================
	// File server code
	// ==================================================

	/**
	 * Serves file from homeDir and its' subdirectories (only).
	 * Uses only URI, ignores all headers and HTTP parameters.

	 public Response serveFile( String uri, Properties header, File homeDir,
	 boolean allowDirectoryListing )
	 {
	 // Make sure we won't die of an exception later
	 if ( !homeDir.isDirectory())
	 return new Response(HTTPStatusCode.HTTP_INTERNALERROR.toString(), MIME_PLAINTEXT,
	 "INTERNAL ERRROR: serveFile(): given homeDir is not a directory." );

	 // Remove URL arguments
	 uri = uri.trim().replace( File.separatorChar, '/' );
	 if ( uri.indexOf( '?' ) >= 0 )
	 uri = uri.substring(0, uri.indexOf( '?' ));

	 // Prohibit getting out of current directory
	 if ( uri.startsWith( ".." ) || uri.endsWith( ".." ) || uri.indexOf( "../" ) >= 0 )
	 return new Response(HTTPStatusCode.HTTP_FORBIDDEN.toString(), MIME_PLAINTEXT,
	 "FORBIDDEN: Won't serve ../ for security reasons." );

	 File f = new File( homeDir, uri );
	 if ( !f.exists())
	 return new Response(HTTPStatusCode.HTTP_NOTFOUND.toString(), MIME_PLAINTEXT,
	 "Error 404, file not found." );

	 // List the directory, if necessary
	 if ( f.isDirectory())
	 {
	 // Browsers get confused without '/' after the
	 // directory, send a redirect.
	 if ( !uri.endsWith( "/" ))
	 {
	 uri += "/";
	 Response r = new Response(HTTPStatusCode.HTTP_REDIRECT.toString(), MIME_HTML,
	 "<html><body>Redirected: <a href=\"" + uri + "\">" +
	 uri + "</a></body></html>");
	 r.addHeader( "Location", uri );
	 return r;
	 }

	 // First try index.html and index.htm
	 if ( new File( f, "index.html" ).exists())
	 f = new File( homeDir, uri + "/index.html" );
	 else if ( new File( f, "index.htm" ).exists())
	 f = new File( homeDir, uri + "/index.htm" );

	 // No index file, list the directory
	 else if ( allowDirectoryListing )
	 {
	 String[] files = f.list();
	 String msg = "<html><body><h1>Directory " + uri + "</h1><br/>";

	 if ( uri.length() > 1 )
	 {
	 String u = uri.substring( 0, uri.length()-1 );
	 int slash = u.lastIndexOf( '/' );
	 if ( slash >= 0 && slash  < u.length())
	 msg += "<b><a href=\"" + uri.substring(0, slash+1) + "\">..</a></b><br/>";
	 }

	 for ( int i=0; i<files.length; ++i )
	 {
	 File curFile = new File( f, files[i] );
	 boolean dir = curFile.isDirectory();
	 if ( dir )
	 {
	 msg += "<b>";
	 files[i] += "/";
	 }

	 msg += "<a href=\"" + encodeUri( uri + files[i] ) + "\">" +
	 files[i] + "</a>";

	 // Show file size
	 if ( curFile.isFile())
	 {
	 long len = curFile.length();
	 msg += " &nbsp;<font size=2>(";
	 if ( len < 1024 )
	 msg += len + " bytes";
	 else if ( len < 1024 * 1024 )
	 msg += len/1024 + "." + (len%1024/10%100) + " KB";
	 else
	 msg += len/(1024*1024) + "." + len%(1024*1024)/10%100 + " MB";

	 msg += ")</font>";
	 }
	 msg += "<br/>";
	 if ( dir ) msg += "</b>";
	 }
	 msg += "</body></html>";
	 return new Response(HTTPStatusCode.HTTP_OK.toString(), MIME_HTML, msg );
	 }
	 else
	 {
	 return new Response(HTTPStatusCode.HTTP_FORBIDDEN.toString(), MIME_PLAINTEXT,
	 "FORBIDDEN: No directory listing." );
	 }
	 }

	 try
	 {
	 // Get MIME type from file name extension, if possible
	 String mime = null;
	 int dot = f.getCanonicalPath().lastIndexOf( '.' );
	 if ( dot >= 0 )
	 mime = (String)theMimeTypes.get( f.getCanonicalPath().substring( dot + 1 ).toLowerCase());
	 if ( mime == null )
	 mime = MIME_DEFAULT_BINARY;

	 // Support (simple) skipping:
	 long startFrom = 0;
	 String range = header.getProperty( "range" );
	 if ( range != null )
	 {
	 if ( range.startsWith( "bytes=" ))
	 {
	 range = range.substring( "bytes=".length());
	 int minus = range.indexOf( '-' );
	 if ( minus > 0 )
	 range = range.substring( 0, minus );
	 try	{
	 startFrom = Long.parseLong( range );
	 }
	 catch ( NumberFormatException nfe ) {}
	 }
	 }

	 FileInputStream fis = new FileInputStream( f );
	 fis.skip( startFrom );
	 Response r = new Response(HTTPStatusCode.HTTP_OK.toString(), mime, fis );
	 r.addHeader( "Content-length", "" + (f.length() - startFrom));
	 r.addHeader( "Content-range", "" + startFrom + "-" +
	 (f.length()-1) + "/" + f.length());
	 return r;
	 }
	 catch( IOException ioe )
	 {
	 return new Response(HTTPStatusCode.HTTP_FORBIDDEN.toString(), MIME_PLAINTEXT, "FORBIDDEN: Reading file failed." );
	 }
	 }
	 */

	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	private static Hashtable theMimeTypes = new Hashtable();

	static {
		StringTokenizer st = new StringTokenizer(
				"css		text/css " +
						"js			text/javascript " +
						"htm		text/html " +
						"html		text/html " +
						"txt		text/plain " +
						"asc		text/plain " +
						"gif		image/gif " +
						"jpg		image/jpeg " +
						"jpeg		image/jpeg " +
						"png		image/png " +
						"mp3		audio/mpeg " +
						"m3u		audio/mpeg-url " +
						"pdf		application/pdf " +
						"doc		application/msword " +
						"ogg		application/x-ogg " +
						"zip		application/octet-stream " +
						"exe		application/octet-stream " +
						"class		application/octet-stream ");
		while (st.hasMoreTokens())
			theMimeTypes.put(st.nextToken(), st.nextToken());
	}

    public int getMyTcpPort() {
        return myTcpPort;
    }

    /**
	 * GMT date formatter
	 */
	private static java.text.SimpleDateFormat gmtFrmt;

	static {
		gmtFrmt = new java.text.SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * The distribution licence
	 */
	private static final String LICENCE =
			"Copyright (C) 2001,2005-2011 by Jarno Elonen <elonen@iki.fi>\n" +
					"and Copyright (C) 2010 by Konstantinos Togias <info@ktogias.gr>\n" +
					"\n" +
					"Redistribution and use in source and binary forms, with or without\n" +
					"modification, are permitted provided that the following conditions\n" +
					"are met:\n" +
					"\n" +
					"Redistributions of source code must retain the above copyright notice,\n" +
					"this list of conditions and the following disclaimer. Redistributions in\n" +
					"binary form must reproduce the above copyright notice, this list of\n" +
					"conditions and the following disclaimer in the documentation and/or other\n" +
					"materials provided with the distribution. The name of the author may not\n" +
					"be used to endorse or promote products derived from this software without\n" +
					"specific prior written permission. \n" +
					" \n" +
					"THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\n" +
					"IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n" +
					"OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n" +
					"IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\n" +
					"INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\n" +
					"NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\n" +
					"DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\n" +
					"THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n" +
					"(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n" +
					"OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
}
