package com.nkdata.gwt.streamer.client.std;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import com.nkdata.gwt.streamer.client.StreamFactory.Reader;
import com.nkdata.gwt.streamer.client.StreamFactory.Writer;
import com.nkdata.gwt.streamer.client.impl.ReadContext;
import com.nkdata.gwt.streamer.client.impl.WriteContext;

public class CollectionStreamers {
	//ArrayList
	public static class ArrayListStreamer extends CollectionStreamer {
		@Override
		protected Collection<Object> createCollectionInstance(int length) {
			return new ArrayList<Object>( length );
		}
	}
	
	// LinkedList
	public static class LinkedListStreamer extends CollectionStreamer {
		@Override
		protected Collection<Object> createCollectionInstance(int length) {
			return new LinkedList<Object>();
		}
	}
	
	// HashSet
	public static class HashSetStreamer extends CollectionStreamer {
		@Override
		protected Collection<Object> createCollectionInstance(int length) {
			return new HashSet<Object>( length*4/3+1 );
		}
	}
	
	// LinkedHashSet
	public static class LinkedHashSetStreamer extends CollectionStreamer {
		@Override
		protected Collection<Object> createCollectionInstance(int length) {
			return new LinkedHashSet<Object>( length*4/3+1 );
		}
	}
	
	
	// TreeSet
	public static class TreeSetStreamer extends CollectionStreamer
	{
		@Override
		protected void writeObjectData(Collection<?> obj, WriteContext ctx, Writer out) 
		{
			get().writeObject( ((TreeSet<?>)obj).comparator(), ctx, out );
			super.writeObjectData(obj, ctx, out);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Object readObject(ReadContext ctx, Reader in)
		{
			Comparator<Object> cmp = (Comparator<Object>) get().readObject( ctx, in );
			int length = in.readInt();
			Collection<Object> obj = new TreeSet<Object>( cmp );
			ctx.addRef( obj );
			readObjectData( obj, ctx, in, length );
			return obj;
		}

		@Override
		protected Collection<Object> createCollectionInstance(int length) {
			throw new RuntimeException();
		}
	}
	
	
	// Vector
	public static class VectorStreamer extends CollectionStreamer {
		@Override
		protected Collection<Object> createCollectionInstance(int length) {
			return new Vector<Object>( length );
		}
	}
	

	// HashMap
	public static class HashMapStreamer extends MapStreamer {
		@Override
		protected Map<Object, Object> createMapInstance( int length ) {
			return new HashMap<Object, Object>( length*4/3+1 );
		}
	}
	
	// IdentityHashMap
	public static class IdentityHashMapStreamer extends MapStreamer {
		@Override
		protected Map<Object, Object> createMapInstance( int length ) {
			return new IdentityHashMap<Object, Object>( length*4/3+1 );
		}
	}
	
	
	// LinkedHashMap
	public static class LinkedHashMapStreamer extends MapStreamer {
		@Override
		protected Map<Object, Object> createMapInstance( int length ) {
			return new LinkedHashMap<Object, Object>( length*4/3+1 );
		}
	}
	
	// TreeMap
	public static class TreeMapStreamer extends MapStreamer 
	{
		@Override
		protected void writeObjectData(Map<?, ?> obj, WriteContext ctx,	Writer out) 
		{
			get().writeObject( ((TreeMap<?,?>)obj).comparator(), ctx, out );
			super.writeObjectData(obj, ctx, out);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Object readObject(ReadContext ctx, Reader in)
		{
			Comparator<Object> cmp = (Comparator<Object>) get().readObject( ctx, in );
			int length = in.readInt();
			Map<Object,Object> obj = new TreeMap<Object, Object>( cmp );
			ctx.addRef( obj );
			readObjectData( obj, ctx, in, length );
			return obj;
		}
		
		@Override
		protected Map<Object, Object> createMapInstance( int length ) {
			throw new RuntimeException();
		}
	}
}
