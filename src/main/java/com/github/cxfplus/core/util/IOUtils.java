/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cxfplus.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class IOUtils {
	private static Logger log = LoggerFactory.getLogger(IOUtils.class);
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	private static final File[] EMPTY = new File[0];

	/**
	 * 关闭指定的对象，不会抛出异常
	 * 
	 * @param input
	 * @deprecated 请使用{@link #closeQuietly}，该方法和Apache commons-io中的工具类同名，更适合代码复用
	 */
	public static void close(Closeable input) {
		closeQuietly(input);
	}
	
	/**
	 * 返回创建文件的流
	 * 
	 * @param file
	 * @return
	 */
	public static BufferedOutputStream getOutputStream(File file) {
		return getOutputStream(file, true);
	}
	
	/**
	 * 返回创建文件的流
	 * 
	 * @param file
	 * @param mode
	 * @return
	 */
	public static BufferedOutputStream getOutputStream(File file, boolean mode) {
		if (file.exists()) {
			if (mode == false) {
				return null;
			} else if (mode == true && file.isDirectory()) {
				throw new IllegalArgumentException("the folder " + file.getAbsolutePath() + " is already exists");
			}
		}
		ensureParentFolder(file);
		try {
			return new BufferedOutputStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * 关闭指定的对象，不会抛出异常
	 * 
	 * @param input
	 */
	public static void closeQuietly(Closeable input) {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				log.error("",e);
			}
		}
	}

	static final void loadProperties(BufferedReader in, Map<String, String> map) {
		if (in == null)
			return;
		try {
			String s;
			while ((s = in.readLine()) != null) {
				if (StringUtils.isBlank(s) || s.startsWith("#"))
					continue;
				int index = s.indexOf("=");
				String key;
				String value;
				if (index > -1) {
					key = s.substring(0, index).trim();
					value = s.substring(index + 1).trim();
				} else {
					key = s.trim();
					value = "";
				}
				if (StringUtils.isEmpty(key))
					continue;
				while (value.endsWith("\\") && (s = in.readLine()) != null) {
					value = value.concat(s);
				}
				map.put(key, value);
			}
		} catch (Exception e1) {
			log.error("",e1);
		} finally {
			closeQuietly(in);
		}
	}

	/**
	 * 获得配置文件的项目。配置文件用=分隔值对，类似于properties文件
	 * 
	 * @param file
	 * @return
	 */
	public static Map<String, String> loadProperties(URL in) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		loadProperties(getReader(in, null), result);
		return result;
	}

	/**
	 * 获得配置文件的项目。配置文件用=分隔值对，类似于properties文件
	 * 
	 * @param file
	 * @return
	 */
	public static Map<String, String> loadProperties(BufferedReader in) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		loadProperties(in, result);
		return result;
	}

	/**
	 * 保存成properties文件
	 * 
	 * @param writer
	 * @param map
	 * @param closeWriter
	 */
	public static void storeProperties(BufferedWriter writer, Map<String, String> map, boolean closeWriter) {
		try {
			for (String key : map.keySet()) {
				writer.write(key);
				writer.write('=');
				writer.write(map.get(key));
				writer.newLine();
			}
			writer.flush();
		} catch (IOException e1) {
			log.error("",e1);
		} finally {
			if (closeWriter)
				closeQuietly(writer);
		}
	}

	/**
	 * 删除文件夹内部的所有内容，文件夹本身不删除。 如果输入一个file，那么总是返回true
	 * 
	 * @param f
	 * @return
	 */
	public static boolean deleteAllChildren(File f) {
		Assert.notNull(f);
		if (!f.exists())
			return true;
		if (f.isDirectory()) {
			for (File sub : listFolders(f)) {
				if (!deleteTree(sub, true))
					return false;
			}
			for (File sub : listFiles(f)) {
				if (!sub.delete())
					return false;
			}
		}
		return true;
	}

	/**
	 * 删除整个文件夹树
	 * 
	 * @param f
	 *            要删除的文件或文件夹
	 * @param includeSub
	 *            如果为false,那么如果目录非空，将不删除。返回false
	 * @return 成功删除返回true,没成功删除返回false。 如果文件夹一开始就不存在，也返回true。
	 */
	public static boolean deleteTree(File f, boolean includeSub) {
		Assert.notNull(f);
		if (!f.exists())
			return true;
		if (includeSub && f.isDirectory()) {
			for (File sub : listFolders(f)) {
				if (!deleteTree(sub, true))
					return false;
			}
			for (File sub : listFiles(f)) {
				if (!sub.delete())
					return false;
			}
		}
		return f.delete();
	}

	/**
	 * 递归列出所有文件夹
	 * 
	 * @param file
	 * @return
	 */
	public static File[] listFoldersRecursive(File file, final FileFilter folderFilter) {
		List<File> files = new ArrayList<File>();
		for (File folder : listFolders(file)) {
			if (folderFilter == null || folderFilter.accept(folder)) {
				files.add(folder);
				files.addAll(Arrays.asList(listFoldersRecursive(folder, folderFilter)));
			}
		}
		return files.toArray(new File[files.size()]);
	}
	
	
	/**
	 * 功能和listFilesRecursive一样，区别在于实现模式不同，迭代器的模式可以减少开销
	 * @param file
	 * @param extnames
	 * @return
	 */
	public static Iterator<File> iterateFilesRecursive(File file, FileFilter filter){
		
		
		
		
		
//		File[] folders=listFolders(file);
		return null;
		
		
	}

	/**
	 * 递归列出指定目录下的文件（不含文件夹）
	 * 
	 * @param file
	 * @param extnames
	 *            允许列出的扩展名，必须小写，不含.号
	 * @return
	 */
	public static File[] listFilesRecursive(File file, final String... extnames) {
		List<File> files = new ArrayList<File>();
		for (File folder : listFolders(file)) {
			files.addAll(Arrays.asList(listFilesRecursive(folder, extnames)));
		}
		files.addAll(Arrays.asList(listFiles(file, extnames)));
		return files.toArray(new File[files.size()]);
	}

	/**
	 * 递归列出指定目录下的文件（不含文件夹）
	 * 
	 * @param file
	 * @param filter
	 *            指定的过滤器
	 * @return
	 */
	public static File[] listFilesRecursive(File file, final FileFilter fileFilter, final FileFilter folderFilter) {
		List<File> files = new ArrayList<File>();
		if (file.exists()) {
			for (File f : file.listFiles()) {
				if (f.isDirectory()) {
					if (folderFilter != null && !folderFilter.accept(f)) {
						continue;
					}
					files.addAll(Arrays.asList(listFilesRecursive(f, fileFilter, folderFilter)));
				} else {
					if (fileFilter != null && !fileFilter.accept(f)) {
						continue;
					}
					files.add(f);
				}
			}
		}
		return files.toArray(new File[files.size()]);
	}


	/**
	 * 列出符合扩展名条件的全部文件
	 * 
	 * @param file
	 * @param extnames
	 * @return
	 */
	public static File[] listFiles(File file, final String... extnames) {
		File[] r = file.listFiles(new FileFilter() {
			public boolean accept(File f) {
				boolean isAll = extnames.length == 0;
				if (f.isFile() && (isAll || ArrayUtils.contains(extnames, getExtName(f.getName())))) {
					return true;
				}
				return false;
			}
		});
		return r == null ? EMPTY : r;
	}

	/**
	 * 列出指定目录下的文件夹
	 * 
	 * @param file
	 * @return
	 */
	public static File[] listFolders(File file) {
		File[] r = file.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				return false;
			}
		});
		return r == null ? EMPTY : r;
	}


	/**
	 * 列出指定目录下的文件和文件夹，其中文件只列出符合扩展名的文件。
	 * 
	 * @param file
	 * @param extnames
	 *            ，允许列出的扩展名，必须小写。不含.号
	 * @return
	 */
	public static File[] listFilesAndFolders(File file, final String... extnames) {
		File[] r = file.listFiles(new FileFilter() {
			public boolean accept(File f) {
				boolean isAll = extnames.length == 0;
				if (f.isDirectory()) {
					return true;
				}
				if (isAll || ArrayUtils.contains(extnames, getExtName(f.getName()))) {
					return true;
				}
				return false;
			}
		});
		return r == null ? EMPTY : r;
	}

	/**
	 * 给定一个File,确认其不存在于在磁盘上，如果存在就改名以回避
	 * 
	 * @param outputFile
	 * @return
	 */
	public static File escapeExistFile(File outputFile) {
		if (!outputFile.exists())
			return outputFile;
		int pos = outputFile.getName().lastIndexOf(".");
		String path = outputFile.getParent();
		if (StringUtils.isEmpty(path)) {
			throw new IllegalArgumentException(outputFile.getAbsolutePath() + " has no valid parent folder.");
		}
		String baseFilename = null;
		String extName = null;
		if (pos > -1) {
			baseFilename = outputFile.getName().substring(0, pos);
			extName = outputFile.getName().substring(pos + 1);
		} else {
			baseFilename = outputFile.getName();
		}
		int n = 1;
		while (outputFile.exists()) {
			outputFile = new File(path + "/" + baseFilename + "(" + n + ")" + ((extName == null) ? "" : "." + extName));
			n++;
		}
		return outputFile;
	}

	/**
	 * 得到文件的扩展名（小写如果没有则返回空字符串）
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getExtName(String fileName) {
		int pos = fileName.lastIndexOf(".");
		if (pos > -1) {
			return fileName.substring(pos + 1).toLowerCase();
		} else {
			return "";
		}
	}

	/**
	 * 得到文件名除去扩展名的部分
	 * 
	 * @param fileName
	 * @return
	 */
	public static String removeExt(String fileName) {
		return StringUtils.substringBeforeLast(fileName, ".");
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param path
	 * @return 如果该文件存在，且不是目录，返回true。否则返回false
	 */
	public static boolean isFile(String path) {
		File f = new File(path);
		return f.exists() && f.isFile();
	}

	/**
	 * 判断目录是否存在
	 * 
	 * @param path
	 * @return 如果该文件存在，且是目录，返回true。否则返回false
	 */
	public static boolean isFolder(String path) {
		File f = new File(path);
		return f.exists() && f.isDirectory();
	}


	/**
	 * 检查/创建 文件夹
	 * 
	 * @param path
	 */
	public static void createFolder(String path) {
		File file = new File(path);
		if (file.exists() && file.isFile()) {
			throw new RuntimeException("Duplicate name file exist. can't create directory " + path);
		} else if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * 检查/创建文件在所的文件夹
	 * 
	 * @param file
	 */
	public static void ensureParentFolder(File file) {
		File f = file.getParentFile();
		if (f != null && !f.exists()) {
			f.mkdirs();
		} else if (f != null && f.isFile()) {
			throw new RuntimeException(f.getAbsolutePath() + " is a exist file, can't create directory.");
		}
	}

	/**
	 * 在reader中读取字符，直到出现指定字符中的任一个，停止读取。 返回的字符串中包含了最后出现的字符
	 * 
	 * @param reader
	 * @param appear
	 * @return
	 * @throws IOException
	 */
	public static String readUntill(Reader reader, char... appear) throws IOException {
		StringBuilder sb = new StringBuilder();
		while (true) {
			int i = reader.read();
			if (i < 0)
				return sb.length() == 0 ? null : sb.toString();
			sb.append((char) i);
			for (int ind = 0; ind < appear.length; ind++) {
				if (appear[ind] == i) {
					return sb.toString();
				}
			}
		}
	}

	/**
	 * 在reader中读取字符，直到出现指定字符中的任一个，停止读取。 返回的字符串中不包含最后出现的字符
	 * 
	 * @param reader
	 * @param appear
	 * @return
	 * @throws IOException
	 */
	public static String readTill(Reader reader, char... appear) throws IOException {
		StringBuilder sb = new StringBuilder();
		while (true) {
			int i = reader.read();
			if (i < 0)
				return sb.length() == 0 ? null : sb.toString();
			for (int ind = 0; ind < appear.length; ind++) {
				if (appear[ind] == i) {
					return sb.toString();
				}
			}
			sb.append((char) i);
		}
	}

	public static String[] readLine(URL in, String charset, int... num) throws IOException {
		BufferedReader is = getReader(in, charset);
		try {
			String line = null;
			if (num.length == 0)
				num = new int[] { -1 };
			boolean isAll = num[0] == -1;
			List<String> result = new ArrayList<String>(isAll ? 20 : num.length);
			int n = 0;
			while ((line = is.readLine()) != null) {
				n++;
				if (isAll || ArrayUtils.contains(num, n)) {
					result.add(line);
				}
				if (!isAll && n >= num[num.length - 1])
					break;
			}
			return result.toArray(new String[result.size()]);
		} finally {
			closeQuietly(is);
		}
	}

	/**
	 * 得到文本文件的某几行，使用后文件会关闭 。
	 * 
	 * @param inName
	 *            要读的文本文件
	 * @param num
	 *            指定的行号,可以指定多行，必须按顺序(如果不指定则表示读取全部行；如果指定的行号小于1，会返回第一行)
	 * @return
	 * @throws IOException
	 */
	public static String[] readLine(File inName, String charset, int... num) throws IOException {
		BufferedReader is = getReader(inName, charset);
		try {
			String line = null;
			if (num.length == 0)
				num = new int[] { -1 };
			boolean isAll = num[0] == -1;
			List<String> result = new ArrayList<String>(isAll ? 20 : num.length);
			int n = 0;
			while ((line = is.readLine()) != null) {
				n++;
				if (isAll || ArrayUtils.contains(num, n)) {
					result.add(line);
				}
				if (!isAll && n >= num[num.length - 1])
					break;
			}
			return result.toArray(new String[result.size()]);
		} finally {
			closeQuietly(is);
		}
	}

	/**
	 * 将文本文件的指定行拼成String返回
	 * 
	 * @param inName
	 * @param charset
	 * @param filter
	 * @return
	 * @throws IOException
	 */
	public static String readLinesAsString(File inName, String charset, LineFilter filter) throws IOException {
		BufferedReader is = getReader(inName, charset);
		String line = null;
		StringBuilder sb = new StringBuilder();
		int n = 0;
		while ((line = is.readLine()) != null) {
			if (filter == null) {
				if (sb.length() > 0)
					sb.append('\n');
				sb.append(line);
			} else {
				String ll = filter.filter(line, n++);
				if (ll != null) {
					if (sb.length() > 0)
						sb.append('\n');
					sb.append(line);
				}
			}
		}
		is.close();
		return sb.toString();
	}

	/**
	 * 行过滤器接口
	 * 
	 * @author Administrator
	 * 
	 */
	public interface LineFilter {
		/**
		 * 过滤行
		 * 
		 * @param line
		 *            传入：行的内容
		 * @param num
		 *            传入：行号
		 * @return 过滤后的行的内容，如果传出null表示不需要这一行
		 */
		String filter(String line, int num);
	}

	/**
	 * 从文件中读取需要的行
	 * 
	 * @param inName
	 * @param filter
	 * @return
	 * @throws IOException
	 */
	public static String[] readLines(File inName, String charset, LineFilter filter) throws IOException {
		BufferedReader is = getReader(inName, charset);
		String line = null;
		List<String> result = new ArrayList<String>();
		int n = 0;
		while ((line = is.readLine()) != null) {
			if (filter != null) {
				String ll = filter.filter(line, n++);
				if (ll != null) {
					result.add(line);
				}
			} else {
				result.add(line);
			}
		}
		is.close();
		return result.toArray(new String[result.size()]);
	}

	/**
	 * 返回文件路径。<BR>
	 * getAbsolutePath 并不是唯一的，比如同一个文件， getAbsolutePath()可以返回 C:/TEMP/../book.exe
	 * ,也可以是 C:/book.exe。 而getCanonicalPath()才可以返回真正的文件路径。 <BR>
	 * 问题是这个方法需要抛出一个受检异常，很多时候影响 代码风格的简洁美观。
	 * 
	 * @param file
	 */
	public static String getPath(File file) {
		Assert.notNull(file);
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			return file.getAbsolutePath();
		}
	}

	/*
	 * Copies the contents of the given {@link InputStream} to the given {@link
	 * OutputStream}.
	 * 
	 * @param pIn The input stream, which is being read. It is guaranteed, that
	 * {@link InputStream#close()} is called on the stream.
	 * 关于InputStram在何时关闭的问题，我一直认为应当是成对操作的（即在哪个方法中生成Stream，就要在使用完后关闭），
	 * 因此不打算在这里使用close方法。 但是后来我又考虑到，InputStream在使用完后，其内部标记已经发生了变化，无法再次使用。
	 * (reset方法的效果和实现有关，并不能保证回复到Stream使用前的状态。)
	 * 因此考虑这里统一关闭以防止疏漏，外面再关一次也不会有问题(作为好习惯，还是应该成对打开和关闭)。
	 * 
	 * @param pOut 输出流，可以为null,此时输入流中的相应数据将丢弃
	 * 
	 * @param pClose True guarantees, that {@link OutputStream#close()} is
	 * called on the stream. False indicates, that only {@link
	 * OutputStream#flush()} should be called finally.
	 * 
	 * @param pBuffer Temporary buffer, which is to be used for copying data.
	 * 
	 * @return Number of bytes, which have been copied.
	 * 
	 * @throws IOException An I/O error occurred.
	 */
	private static long copy(InputStream in, OutputStream out, boolean inClose, boolean outClose, byte[] pBuffer) throws IOException {
		if (in == null)
			throw new NullPointerException();
		long total = 0;
		try {
			int res;
			while ((res = in.read(pBuffer)) != -1) {
				if (out != null) {
					out.write(pBuffer, 0, res);
				}
				total += res;
			}
			if (out != null)
				out.flush();
		} finally {
			if (outClose)
				closeQuietly(out);
			if (inClose)
				closeQuietly(in);
		}
		return total;
	}

	/*
	 * 同上、READER和Writer之间的拷贝
	 */
	private static long copy(Reader in, Writer out, boolean inClose, boolean outClose, char[] pBuffer) throws IOException {
		if (in == null)
			throw new NullPointerException();
		long total = 0;
		try {
			int res;
			while ((res = in.read(pBuffer)) != -1) {
				if (out != null) {
					out.write(pBuffer, 0, res);
				}
				total += res;
			}
			if (out != null)
				out.flush();
		} finally {
			if (outClose && out != null)
				closeQuietly(out);
			if (inClose)
				closeQuietly(in);
		}
		return total;
	}

	/**
	 * 流之间拷贝
	 * 
	 * @param in
	 *            输入
	 * @param out
	 *            输出
	 * @param inClose
	 *            关闭输入流？
	 * @param outClose
	 *            关闭输出流?
	 * @return
	 * @throws IOException
	 */
	public static long copy(InputStream in, OutputStream out, boolean inClose, boolean outClose) throws IOException {
		return copy(in, out, inClose, outClose, new byte[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * 流之间拷贝
	 * 
	 * @param pInputStream
	 * @param pOutputStream
	 * @param pClose
	 *            关闭输出流? (输入流默认关闭)
	 * @return
	 * @throws IOException
	 */
	public static long copy(InputStream pInputStream, OutputStream pOutputStream, boolean pClose) throws IOException {
		return copy(pInputStream, pOutputStream, true, pClose, new byte[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * 将Reader内容读取到内存中的charArray
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static char[] asCharArray(Reader reader) throws IOException {
		CharArrayWriter cw = new CharArrayWriter(256);
		char[] buf = new char[1024];
		int n;
		try {
			while ((n = reader.read(buf)) > -1) {
				cw.write(buf, 0, n);
			}
		} finally {
			reader.close();
		}
		return cw.toCharArray();
	}

	/**
	 * 将Reader的内容读取为一个字符串
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static String asString(Reader reader) throws IOException {
		return asString(reader, true);
	}

	/**
	 * 将Reader内容读取为字符串
	 * 
	 * @param reader
	 * @param close
	 *            关闭reader
	 * @return
	 * @throws IOException
	 */
	public static String asString(Reader reader, boolean close) throws IOException {
		if (reader == null)
			return null;
		StringBuilder sb = new StringBuilder(128);
		char[] buf = new char[1024];
		int n;
		try {
			while ((n = reader.read(buf)) > -1) {
				sb.append(buf, 0, n);
			}
		} finally {
			if (close)
				reader.close();
		}
		return sb.toString();
	}

	public static String asString(File pStream, String charset) throws IOException {
		return asString(getReader(pStream, charset));
	}

	public static String asString(URL filepath, String charset) throws IOException {
		if (filepath == null)
			return null;
		return asString(filepath.openStream(), charset, true);
	}

	/**
	 * 将输入流转化为String .(使用缺省的字符集)
	 * 
	 * 最简单的获取系统资源转换为String的写法如下： <code>
	 *    IOUtils.asString(ClassLoader.getSystemResourceAsStream(filename))
	 * </code>
	 */
	public static String asString(InputStream pStream) throws IOException {
		return asString(pStream, null, true);
	}

	/**
	 * 将输入流转化为String
	 * 
	 * @param pStream
	 *            The input stream to read.
	 * @param pEncoding
	 *            The character encoding, typically "UTF-8".
	 * @param close
	 *            close the in stream?
	 */
	public static String asString(InputStream pStream, String pEncoding, boolean close) throws IOException {
		if (pStream == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
		copy(pStream, baos, close);
		if (pEncoding == null) {
			return baos.toString();
		} else {
			return baos.toString(pEncoding);
		}
	}

	public static byte[] toByteArray(URL file) throws IOException {
		return toByteArray(file.openStream());
	}

	/**
	 * 读取流数据到内存。注意这个方法会将数据流全部读入到内存中，因此不适用于很大的数据对象
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray(InputStream in) throws IOException {
		try {
			byte[] msg = toByteArray(in, -1);
			return msg;
		} finally {
			in.close();
		}
	}

	/**
	 * @deprecated use {@link #toByteArray(InputStream)}
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static byte[] asByteArray(InputStream in) throws IOException {
		return toByteArray(in);
	}

	/**
	 * 将内存数据转换为流
	 * 
	 * @param bytes
	 * @return
	 */
	public static InputStream asInputStream(byte[] bytes) {
		return new ByteArrayInputStream(bytes);
	}

	/**
	 * 从流中读取指定的字节，第二个版本，性能比第一版本有明显提升，但比起第三次修改明显不如，仅作参考保留
	 * 
	 * @param in
	 * @param length
	 * 
	 * @return
	 * @throws IOException
	 * @deprecated
	 */
	public static byte[] toByteArray_v2(InputStream in, int length) throws IOException {
		ByteArrayOutputStream out;
		if (length > 0) {
			out = new ByteArrayOutputStream(length);
		} else {
			out = new ByteArrayOutputStream(1024);
		}
		byte[] pBuffer = new byte[DEFAULT_BUFFER_SIZE];
		int left = (length > 0) ? length : Integer.MAX_VALUE;// 剩余字节数
		while (left > 0) {
			int n;
			if (left < DEFAULT_BUFFER_SIZE) {
				n = in.read(pBuffer, 0, left);
			} else {
				n = in.read(pBuffer);
			}
			if (n == -1)
				break;
			left -= n;
			out.write(pBuffer, 0, n);
		}
		out.close();
		byte[] message = out.toByteArray();
		return message;
	}

	/**
	 * asByteArray的旧版本，重写后实际测试发现性能要低5%～10%。 仅为参考保留
	 * 
	 * @deprecated
	 * @param in
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray_old(InputStream in, int length) throws IOException {
		ByteArrayOutputStream out;
		if (length > 0) {
			out = new ByteArrayOutputStream(length);
		} else {
			out = new ByteArrayOutputStream(1024);
		}
		byte[] pBuffer = new byte[DEFAULT_BUFFER_SIZE];
		int count = 0;
		while (count < length || length < 0) {
			if (length < 0 || length - count > DEFAULT_BUFFER_SIZE) {
				int n = in.read(pBuffer);
				if (n == -1)
					break;
				count += n;
				out.write(pBuffer, 0, n);
			} else {
				int n = in.read(pBuffer, 0, length - count);
				if (n == -1)
					break;
				count += n;
				out.write(pBuffer, 0, n);
			}
		}
		out.close();
		byte[] message = out.toByteArray();
		return message;
	}

	/**
	 * 这个方法是JDK InputStream.read(byte b[], int off, int len) 的代替方法
	 * 对于网络数据流，可以反复重试，直到读取到足够的数据
	 * 
	 * @return
	 */
	public static int readBytes(InputStream in, byte[] data, int offset, int length) throws IOException {
		if (length < 0)
			throw new IOException("This method just for reading bytes of a expected length from stream.The param  length must >=0");
		if (length == 0)
			return 0;
		if (offset + length > data.length) {
			throw new IOException("the container byte[] does not enough for the expected length.");
		}
		int left = length;
		int off = offset;
		while (left > 0) {
			int n;
			n = in.read(data, off, left);
			if (n == -1)
				break;
			left -= n;
			off += n;
		}
		return off - offset;
	}

	/**
	 * 合并字节数组
	 * 
	 * @param packages
	 * @return
	 */
	public static byte[] mergeBytes(List<byte[]> packages) {
		int len = 0;
		for (byte[] aPackage : packages) {
			len += aPackage.length;
		}
		byte[] result = new byte[len];
		int pos = 0;
		for (byte[] aPackage : packages) {
			for (int j = 0; j < aPackage.length; j++) {
				result[pos++] = aPackage[j];
			}
		}
		return result;
	}

	/**
	 * 从流中读取指定的字节，第三个版本，性能再度提升 参考数据，从120M文件中读取前60M，此方法耗时125ms,v2耗时156ms
	 * 
	 * @param in
	 * @param length
	 *            要读取的字节数，-1表示不限制。（注意实际处理中-1的情况下最多读取2G数据，超过2G不会读取）
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray(InputStream in, int length) throws IOException {
		ByteArrayOutputStream out;
		if (length > 0) {
			out = new ByteArrayOutputStream(length);
		} else {
			out = new ByteArrayOutputStream(1024);
		}
		int buf = DEFAULT_BUFFER_SIZE;
		byte[] pBuffer = new byte[buf];
		int left = (length > 0) ? length : Integer.MAX_VALUE;// 剩余字节数
		while (left >= buf) {
			int n = in.read(pBuffer);
			if (n == -1) {
				left = 0;
				break;
			}
			left -= n;
			out.write(pBuffer, 0, n);
		}
		while (left > 0) {
			int n = in.read(pBuffer, 0, left);
			if (n == -1) {
				break;
			}
			left -= n;
			out.write(pBuffer, 0, n);
		}
		out.close();// ByteArrayOut其实是不需要close的，这里close是为了防止一些代码检查工具提出警告
		byte[] message = out.toByteArray();
		return message;
	}

	/**
	 * @deprecated use {@link #toByteArray(InputStream, int)}
	 * @param in
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public static byte[] asByteArray(InputStream in, int length) throws IOException {
		return toByteArray(in, length);
	}

	/*
	 * @param pBuffer byte[] 的Bufferr，外部初始化一次，免去每次在此方法中创建数组的开销
	 * 
	 * @param in 数据来源
	 * 
	 * @param out 数据去向（一般为Buffer或byte[]等）
	 * 
	 * @param limit 总共要读取的字节数
	 * 
	 * @param current 当前已读取字节数
	 * 
	 * @return 本次读取的字节数
	 * 
	 * @throws IOException
	 */
	private static int processDataRead(byte[] pBuffer, InputStream in, OutputStream out, long limit, long current) throws IOException {
		int n = 0;
		if (limit < 0 || limit - current > DEFAULT_BUFFER_SIZE) {
			n = in.read(pBuffer);
		} else {
			int left = (int) (limit - current);
			n = in.read(pBuffer, 0, left);
		}
		if (n < 0) {
			return -1; // 没有数据
		} else {// 正常读取,返回本次读取字节数
			out.write(pBuffer, 0, n);
			return n;
		}
	}

	/**
	 * 将指定的流保存为临时文件
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static File saveAsTempFile(InputStream is) throws IOException {
		File f = File.createTempFile("~tmp", ".io");
		saveAsFile(f, is);
		return f;
	}

	/**
	 * 将数据从来源保存为临时文件
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static File saveAsTempFile(Reader reader) throws IOException {
		File f = File.createTempFile("~tmp", ".io");
		saveAsFile(f, null, reader);
		return f;
	}

	/**
	 * 将输入流保存为文件
	 * 
	 * @param is
	 * @param file
	 * @throws IOException
	 */
	public static void saveAsFile(File file, InputStream... iss) throws IOException {
		ensureParentFolder(file);
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
		try {
			for (InputStream is : iss) {
				copy(is, os, false);
			}
		} finally {
			if (os != null) {
				os.flush();
				os.close();
			}
		}
	}

	/**
	 * 将reader内容保存为文件
	 * 
	 * @param reader
	 * @param file
	 * @throws IOException
	 */
	public static void saveAsFile(File file, Charset charset, Reader... readers) throws IOException {
		BufferedWriter os = getWriter(file, charset==null?null:charset.name(),false);
		try {
			for (Reader reader : readers) {
				copy(reader, os, true, false, new char[2048]);
			}
		} finally {
			closeQuietly(os);
		}
	}

	/**
	 * 将文字写入文件
	 * 
	 * @param text
	 * @param file
	 * @param append
	 * @throws IOException
	 */
	public static void saveAsFile(File file, Charset charset, String... texts) throws IOException {
		BufferedWriter os = getWriter(file, charset==null?null:charset.name(),false);
		try {
			for (String text : texts) {
				os.write(text);
			}
		} finally {
			if (os != null) {
				os.flush();
				os.close();
			}
		}
	}

	public static void saveAsFile(File file, String... texts) throws IOException {
		saveAsFile(file, null, texts);
	}

	/**
	 * 将内存数据块写入文件
	 * 
	 * @param data
	 * @param file
	 * @throws IOException
	 */
	public static void saveAsFile(File file, boolean append, byte[] data) throws IOException {
		ensureParentFolder(file);
		OutputStream out = new FileOutputStream(file, append);
		try {
			out.write(data);
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

	/**
	 * 将内存数据块写入文件
	 * 
	 * @param file
	 * @param data
	 * @throws IOException
	 */
	public static void saveAsFile(File file, byte[] data) throws IOException {
		saveAsFile(file, false, data);
	}

	/**
	 * 文件合并 实现方式，使用nio操作。
	 * 
	 * @param list
	 * @param path
	 * @param srcFileName
	 * @return
	 */
	public static boolean combine(Collection<String> list, String path, String srcFileName) {
		File outputFile = new File(path + "/" + srcFileName);
		outputFile = escapeExistFile(outputFile);
		try {
			FileOutputStream fou = new FileOutputStream(outputFile);
			FileChannel fco = fou.getChannel();
			long position = 0;
			for (String i : list) {// 按顺序获得各个文件名
				File file = new File(i);// 创建文件
				if (!file.exists())
					return false;
				FileInputStream fin = new FileInputStream(file);
				FileChannel fci = fin.getChannel();
				long len = file.length();
				fco.transferFrom(fci, position, len);// 接收数据到指定的位置
				position += len;
				closeQuietly(fin);
				closeQuietly(fci);
			}
			closeQuietly(fou);
			closeQuietly(fco);
			return true;
		} catch (Exception ee) {
			log.error("",ee);
			return false;
		}
	}

	/**
	 * 
	 * @param file
	 * @param newName
	 * @return 如果成功改名，返回改名后的file对象，否则返回null
	 */
	public static File rename(File file, String newName, boolean overwite) {
		File target = new File(file.getParentFile(), newName);
		if (target.exists()) {
			if (overwite) {
				if (!target.delete())
					return null;
			} else {
				return null;
			}
		}
		return file.renameTo(target) ? target : null;
	}

	public static abstract class FileFilterEx implements FileFilter {
		/**
		 * 是否跳出当前文件夹搜索
		 * 
		 * @return
		 */
		protected boolean breakFolder(File root) {
			return false;
		};
	}

	/**
	 * 总是被继承，用于描述对文件夹的处理
	 * 
	 * @Company: Asiainfo-Linkage Technologies(China),Inc. Hangzhou
	 * @author Administrator
	 * @Date 2011-6-15
	 */
	public abstract static class FolderCallback {
		protected abstract void process(File source);

		protected boolean breakProcess() {
			return false;
		}
	}

	/**
	 * 从URL获得reader
	 * 
	 * @param file
	 * @param charSet
	 * @return
	 * @throws IOException
	 */
	public static BufferedReader getReader(URL file, String charSet) {
		if (file == null)
			return null;
		try {
			InputStream is = file.openStream();
			InputStreamReader isr = new InputStreamReader(is, charSet==null?Charset.defaultCharset().name():charSet);
			return new BufferedReader(isr);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获得一个供读取文本的reader, 本方法可以从BOM自动判断utf-8, unicode等类型，因此charset一般可以为null.
	 * 只有当文件为不带BOM的unicode时才需要指定。
	 * 
	 * @param source
	 * @param charSet
	 * @return
	 * @throws IOException
	 */
	public static BufferedReader getReader(File file, String charSet) throws IOException {
		if (file == null)
			return null;
		InputStream is = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(is, charSet==null?Charset.defaultCharset().name():charSet);
		return new BufferedReader(isr);
	}

	/**
	 * 获得Reader
	 * 
	 * @param is
	 * @param charSet
	 * @return
	 * @throws IOException
	 */
	public static BufferedReader getReader(InputStream is, String charSet) {
		if (is == null)
			return null;
		try {
			InputStreamReader isr = new InputStreamReader(is, charSet==null?Charset.defaultCharset().name():charSet);
			return new BufferedReader(isr);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * 获得相对于一个class的所在路径的相对路径的文件资源
	 * 
	 * @param source
	 *            class
	 * @param fileName
	 *            文件相对路径
	 * @param charSet
	 *            编码
	 * @return BufferedReader 如果文件不存在，返回null
	 */
	public static BufferedReader getReader(Class<?> source, String fileName, String charSet) {
		InputStream is = source.getResourceAsStream(fileName);
		if (is == null) {
			is = source.getClassLoader().getResourceAsStream(toClassLoaderResourcePath(source, fileName));
		}
		if (is == null)
			return null;
	
		try {
			InputStreamReader isr = new InputStreamReader(is, charSet==null?Charset.defaultCharset().name():charSet);
			return new BufferedReader(isr);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * ClassLoader resource不用/开头。这里将c转换过去
	 * 
	 * @param fileName
	 * @return
	 */
	public static String toClassLoaderResourcePath(Class<?> c, String fileName) {
		if (fileName.startsWith("/"))
			return fileName.substring(1);
		String path = c.getPackage().getName().replace('.', '/');
		return path.concat("/").concat(fileName);
	}

	/**
	 * 类加载器的资源路径url，转换
	 * 
	 * @param fileName
	 * @return
	 */
	public static String toClassResourcePath(String fileName) {
		if (fileName.startsWith("/"))
			return fileName;
		return "/".concat(fileName);
	}

	public static BufferedInputStream getInputStream(URL url) throws IOException {
		URLConnection conn = url.openConnection();
		return new BufferedInputStream(conn.getInputStream());
	}
	
	public static BufferedInputStream getInputStream(File file) throws IOException {
		return new BufferedInputStream(new FileInputStream(file));
	}

	/**
	 * 获得文本文件写入流
	 * 
	 * @param target
	 * @param charSet
	 * @param append
	 * @return
	 * @throws IOException
	 */
	public static BufferedWriter getWriter(File target, String charSet, boolean append) {
		ensureParentFolder(target);
		try {
			OutputStream os = new FileOutputStream(target, append);
			if (charSet == null)
				charSet = Charset.defaultCharset().name();
			OutputStreamWriter osw = new OutputStreamWriter(os, charSet);
			return new BufferedWriter(osw);
		}catch(IOException e){
			throw new RuntimeException(e);
		}

	}

	/**
	 * 获得文本文件写入流
	 * 
	 * @param target
	 * @param charSet
	 * @return
	 * @throws IOException
	 */
	public static BufferedWriter getWriter(File target, String charSet) throws IOException {
		return getWriter(target, charSet , false);
	}
	
	/**
	 * 将OutputStream封装为BufferedWriter
	 * @param out
	 * @param charSet
	 * @return
	 */
	public static BufferedWriter getWriter(OutputStream out, String charSet) {
		if (charSet == null)
			charSet = Charset.defaultCharset().name();
		OutputStreamWriter osw;
		try {
			osw = new OutputStreamWriter(out, charSet);
			return new BufferedWriter(osw);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * 将对象序列化在内存中
	 * 
	 * @param obj
	 * @return
	 */
	public static byte[] serialize(Serializable obj) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		saveObject(obj, out);
		return out.toByteArray();
	}

	/**
	 * 将可序列化的对象存储到流中
	 * 
	 * @param obj
	 * @param output
	 * @return
	 */
	public static boolean saveObject(Serializable obj, OutputStream output) {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(output);
			out.writeObject(obj);
			return true;
		} catch (IOException ex) {
			log.error("",ex);
			return false;
		} finally {
			closeQuietly(out);
		}
	}

	/**
	 * 将可序列化的对象转换到字节数组
	 * 
	 * @param obj
	 * @return
	 */
	public static byte[] saveObject(Serializable obj) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(2048);
		try {
			ObjectOutputStream out = new ObjectOutputStream(bytes);
			out.writeObject(obj);
			closeQuietly(out);
			return bytes.toByteArray();
		} catch (IOException ex) {
			log.error("",ex);
			throw new RuntimeException(ex.getMessage());
		}
	}

	/**
	 * 将可序列化的对象保存到磁盘文件
	 * 
	 * @param obj
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static boolean saveObject(Serializable obj, File file) {
		try {
			return saveObject(obj, new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			log.error("",e);
			return false;
		}
	}

	/**
	 * 将可序列化的对象保存到磁盘文件
	 * 
	 * @param aaa
	 * @param filePath
	 * @return
	 */
	public static boolean saveObject(Serializable aaa, String filePath) {
		return saveObject(aaa, new File(filePath));
	}

	/**
	 * 相对路径计算，计算从folder出发，到达file的相对路径
	 * 
	 * @param file
	 * @param folder
	 * @return
	 */
	public static String getRelativepath(String file, String folder) {
		String[] f1 = StringUtils.split(file.replace('/', '\\'), '\\');
		String[] f2 = StringUtils.split(folder.replace('/', '\\'), '\\');
		int breakCount = -1;
		for (int i = 0; i < f1.length; i++) {
			String str = f1[i];
			if (i < f2.length && str.equals(f2[i])) {
				breakCount = i + 1;
			} else {
				break;
			}
		}
		if (breakCount == -1)
			return file;
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.repeat("../", f2.length - breakCount));
		for (int i = breakCount; i < f1.length; i++) {
			sb.append(f1[i] + "/");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * 相对路径计算，计算从folder出发，到达file的相对路径
	 * 
	 * @param file
	 * @param folder
	 */
	public static String getRelativepath(File file, File folder) {
		String s1 = getPath(file);
		String s2 = getPath(folder);
		return getRelativepath(s1, s2);
	}

	/**
	 * 将ByteBuffer对象脱壳，得到byte[]
	 * 
	 * @param bf
	 * @return
	 */
	public static byte[] toByteArray(ByteBuffer bf) {
		if (bf.position() != 0) {
			throw new RuntimeException("This method only allow you to read a ByteBuffer from start");
		}
		if (bf.limit() == bf.capacity()) {
			return bf.array();
		}
		byte[] bb = new byte[bf.limit()];
		bf.get(bb);
		return bb;
	}

	/**
	 * 在指定目录下查找文件
	 * 
	 * @Title: findFile
	 * @param root
	 * @param filter
	 *            过滤条件
	 * @return File 返回类型
	 * @throws
	 */
	public static File findFile(File root, FileFilterEx filter) {
		if (root == null || !root.exists())
			return null;
		boolean breakThisFolder = false;
		for (File f : root.listFiles()) {
			if (!breakThisFolder) {
				if (filter.accept(f)) {
					return f;
				}
				breakThisFolder = filter.breakFolder(root);
			}
			if (f.isDirectory()) {
				File result = findFile(f, filter);
				if (result != null)
					return result;
			}
		}
		return null;
	}

	/**
	 * 在指定目录下查找文件
	 * 
	 * @param root
	 * @param filter
	 * @return
	 */
	public static Collection<File> findFiles(File root, FileFilterEx filter) {
		if (root == null || !root.exists())
			return null;
		List<File> result = new ArrayList<File>();
		boolean breakThisFolder = false;
		for (File f : root.listFiles()) {
			if (!breakThisFolder) {
				if (filter.accept(f)) {
					result.add(f);
				}
				breakThisFolder = filter.breakFolder(root);
			}
			if (f.isDirectory()) {
				result.addAll(findFiles(f, filter));
			}
		}
		return result;
	}

	/**
	 * 在指定目录下查找文件
	 * 
	 * @Title: findFile
	 * @param root
	 *            查找目录
	 * @param name
	 *            查找的文件名称（完全匹配）
	 * @param acceptFolder
	 *            是否查找文件夹
	 * @return File 返回类型
	 * @throws
	 */
	public static File findFile(File root, final String name, final boolean acceptFolder) {
		return findFile(root, new FileFilterEx() {
			public boolean accept(File pathname) {
				if (!acceptFolder && pathname.isDirectory())
					return false;
				return pathname.getName().equals(name);
			}
		});
	}

	/**
	 * 比较两个文件是否内容一致
	 * @param origin
	 * @param file
	 * @return
	 */
	public static boolean equals(File origin, File file) {
		return FileComparator.LENGTH_SKIP.equals(origin, file);
	}
	
	private static final String FOLDER_SEPARATOR = "/";

	private static final String WINDOWS_FOLDER_SEPARATOR = "\\";
	
	private static final String TOP_PATH = "..";

	private static final String CURRENT_PATH = ".";
	
	/**
	 * Normalize the path by suppressing sequences like "path/.." and
	 * inner simple dots.
	 * <p>The result is convenient for path comparison. For other uses,
	 * notice that Windows separators ("\") are replaced by simple slashes.
	 * @param path the original path
	 * @return the normalized path
	 */
	public static String cleanPath(String path) {
		if (path == null) {
			return null;
		}
		String pathToUse = StringUtils.replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

		// Strip prefix from path to analyze, to not treat it as part of the
		// first path element. This is necessary to correctly parse paths like
		// "file:core/../core/io/Resource.class", where the ".." should just
		// strip the first "core" directory while keeping the "file:" prefix.
		int prefixIndex = pathToUse.indexOf(":");
		String prefix = "";
		if (prefixIndex != -1) {
			prefix = pathToUse.substring(0, prefixIndex + 1);
			pathToUse = pathToUse.substring(prefixIndex + 1);
		}
		if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
			prefix = prefix + FOLDER_SEPARATOR;
			pathToUse = pathToUse.substring(1);
		}

		String[] pathArray = StringUtils.split(pathToUse, FOLDER_SEPARATOR);
		List<String> pathElements = new LinkedList<String>();
		int tops = 0;

		for (int i = pathArray.length - 1; i >= 0; i--) {
			String element = pathArray[i];
			if (CURRENT_PATH.equals(element)) {
				// Points to current directory - drop it.
			}
			else if (TOP_PATH.equals(element)) {
				// Registering top path found.
				tops++;
			}
			else {
				if (tops > 0) {
					// Merging path element with element corresponding to top path.
					tops--;
				}
				else {
					// Normal path element found.
					pathElements.add(0, element);
				}
			}
		}

		// Remaining top paths need to be retained.
		for (int i = 0; i < tops; i++) {
			pathElements.add(0, TOP_PATH);
		}

		return prefix + StringUtils.join(pathElements, FOLDER_SEPARATOR);
	}
}
