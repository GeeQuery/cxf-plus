package com.github.cxfplus.common;

import java.io.File;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import jef.tools.IOUtils;

/**
 * Jaxws适配器。可以将File类型的参数转为byte[]使用XML进行传输.
 * 只传输文件内容，文件路径将被忽略。
 * @author jiyi
 *
 */
public class FileContentCodec extends XmlAdapter<byte[],java.io.File>{
	public File unmarshal(byte[] v) throws Exception {
		if(v==null)return null;
		File file=File.createTempFile("~wstemp", "tmp");
		IOUtils.saveAsFile(file, v);
		return file;
	}

	public byte[] marshal(File v) throws Exception {
		if(v.isFile()){
			return IOUtils.toByteArray(v);
		}else{
			return null;
		}
	}
}
