package com.sioo.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sioo.log.LogInfo;

public class FileUtils {
	private static Logger log = Logger.getLogger(FileUtils.class);

	/***
	 * 读取上次未处理完的用户消费记录文件
	 * 
	 * @return
	 */
	public static Map<Integer, Integer> readSmsCancheTxtFile() {
		Map<Integer, Integer> map = null;
		InputStreamReader read = null;
		BufferedReader bufferedReader = null;
		try {
			File file = new File("/home/sioowork/hy_middle_services/smsCache.txt");
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				log.info("用户消费短信记录文件存在");
				map = new HashMap<Integer, Integer>();
				read = new InputStreamReader(new FileInputStream(file), "utf-8");
				bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					String s = lineTxt;
					if (s != null && s.indexOf(",") != -1) {
						String[] arrays = s.split(",");
						map.put(Integer.parseInt(arrays[0]), Integer.parseInt(arrays[1]));
						continue;
					}
				}

				if (file.exists()) {
					log.info("删除用户消费短信记录文件");
					file.delete();
				}
			}
		} catch (Exception e) {
			log.error("读取用户消费记录失败，file:/home/sioowork/hy_middle_services/smsCache.txt \r\n" + LogInfo.getTrace(e));
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (read != null) {
					read.close();
				}
			} catch (IOException e) {
				log.error("读取用户消费记录，关闭IO异常，");
			}
		}
		return map;
	}

	/***
	 * 保存未处理的用户消费记录
	 * 
	 * @param map
	 */
	public static void saveSmsCancheTxtFile(Map<Integer, Integer> map) {
		FileWriter fw = null;
		try {
			File oldfile = new File("/home/sioowork/hy_middle_services/smsCache.txt");
			if (oldfile.exists()) {
				log.info("删除老的用户消费短信记录文件");
				oldfile.delete();
			}
			
			File file = new File("/home/sioowork/hy_middle_services/smsCache.txt");
			fw = new FileWriter(file, false); // 设置成true就是追加
			for (Integer key : map.keySet()) {
				fw.write(key + "," + map.get(key));
				fw.write("\r\n");
			}
		} catch (Exception e) {
			log.error("保存用户消费记录失败，\r\n" + LogInfo.getTrace(e));
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				log.error("保存用户消费记录，关闭IO异常，");
			}
		}
	}
}
