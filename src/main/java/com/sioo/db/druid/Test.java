package com.sioo.db.druid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Test {

	public static void main(String[] args) {
		Map<Integer,AtomicLong> map = new ConcurrentHashMap<Integer,AtomicLong>();
		map.put(30032, new AtomicLong(10000));
		
		System.out.println(map.get(30032).intValue());
		map.get(30032).addAndGet(-1L);
		System.out.println(map.get(30032).intValue());
	}

}
