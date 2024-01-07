/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class myIO {

	public static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	/**
	 * ファイル出力.
	 * @param value 値
	 * @param filepath ファイルパス
	 */
	public static <T> void write(T value, Path filepath) {
		try (FileOutputStream fos = new FileOutputStream(filepath.toFile())) {
			mapper.writeValue(fos, value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ファイル入力.
	 * @param filepath ファイルパス
	 * @param valueType タイプ
	 * @return 値
	 */
	public static <T> T read(Path filepath, Class<T> valueType) {
		T result = null;
		try (FileInputStream fis = new FileInputStream(filepath.toFile())) {
			result = mapper.readValue(fis, valueType);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
