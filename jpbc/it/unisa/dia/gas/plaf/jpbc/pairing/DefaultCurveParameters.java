package it.unisa.dia.gas.plaf.jpbc.pairing;

import it.unisa.dia.gas.jpbc.CurveParameters;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * TODO: introduce immutable...
 *
 * @author Angelo De Caro (angelo.decaro@gmail.com)
 */
public class DefaultCurveParameters implements CurveParameters {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8972478861894383890L;
	protected final LinkedHashMap<String, String> parameters;


    public DefaultCurveParameters() {
        this.parameters = new LinkedHashMap<String, String>();
    }


    public String getType() {
        return parameters.get("type");
    }


    public boolean containsKey(String key) {
        return parameters.containsKey(key);
    }

    public int getInt(String key) {
        String value = parameters.get(key);
        if (value == null)
            throw new IllegalArgumentException("Cannot find value for the following key : " + key);

        return Integer.parseInt(value);
    }

    public int getInt(String key, int defaultValue) {
        String value = parameters.get(key);
        if (value == null)
            return defaultValue;

        return Integer.parseInt(value);
    }

    public long getLong(String key) {
        String value = parameters.get(key);
        if (value == null)
            throw new IllegalArgumentException("Cannot find value for the following key : " + key);

        return Long.parseLong(value);
    }

    public long getLong(String key, long defaultValue) {
        String value = parameters.get(key);
        if (value == null)
            return defaultValue;

        return Long.parseLong(value);
    }

    public BigInteger getBigInteger(String key) {
        String value = parameters.get(key);
        if (value == null)
            throw new IllegalArgumentException("Cannot find value for the following key : " + key);

        return new BigInteger(value);
    }

    public BigInteger getBigInteger(String key, BigInteger defaultValue) {
        String value = getString(key);
        if (value == null)
            return defaultValue;

        return new BigInteger(value);
    }

    public String getString(String key) {
        String value = parameters.get(key);
        if (value == null)
            throw new IllegalArgumentException("Cannot find value for the following key : " + key);

        return value;
    }

    public String getString(String key, String defaultValue) {
        String value = parameters.get(key);
        if (value == null)
            return defaultValue;

        return value;
    }

    public byte[] getBytes(String key) {
        return Base64.decode(getString(key)).getBytes();
    }

    public byte[] getBytes(String key, byte[] defaultValue) {
        String value = parameters.get(key);
        if (value == null)
            return defaultValue;

        return Base64.decode(value).getBytes();
    }

    public void putBytes(String key, byte[] bytes) {
        parameters.put(key, Base64.encodeBytes(bytes, 0, bytes.length));
    }

    public class DataReader{
    	String data = null;
    	int curr = 0;
    	
    	public DataReader(String data){
    		this.data = data;
    		curr = 0;
    	}
    	
    	public String getLine(){
    		String tmp="";
    		int start = curr;
    		byte[] bytes = data.getBytes();
    		while (curr<bytes.length && bytes[curr]!='\n' && bytes[curr]!='\r'){
    			curr++;
    		}
    		if (curr==bytes.length) return null;
    		
    		tmp=data.substring(start, curr);
    		
    		if (bytes[curr]=='\r' && curr<bytes.length-1 && bytes[curr+1]=='\n'){
    			curr++;
    		}
    		curr++;
			return tmp;
    	}
    }

    public DefaultCurveParameters load(String data) {
        //BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    	DataReader reader = new DataReader(data);
        try {
            while (true) {
                String line = reader.getLine();//reader.readLine();
                if (line == null)
                    break;
                line = line.trim();
                if (line.length() == 0)
                    continue;
                if (line.startsWith("#"))
                    continue;

                StringTokenizer tokenizer = new StringTokenizer(line, "= :", false);
                String key = tokenizer.nextToken();
                String value = tokenizer.nextToken();

                parameters.put(key, value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    /*public DefaultCurveParameters load(String path) {
        InputStream inputStream;

        File file = new File(path);
        if (file.exists()) {
            try {
                inputStream = file.toURI().toURL().openStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        }

        if (inputStream == null)
            throw new IllegalArgumentException("No valid resource found!");

        load(inputStream);

        try {
            inputStream.close();
        } catch (IOException e) {
            // Discard
        }

        return this;
    }*/


    public String toString(String separator) {
        StringBuilder buffer = new StringBuilder();

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            buffer.append(entry.getKey()).append(separator).append(entry.getValue()).append("\n");
        }

        return buffer.toString();
    }

    public String toString() {
        return toString(" ");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultCurveParameters that = (DefaultCurveParameters) o;

        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return parameters != null ? parameters.hashCode() : 0;
    }

    public void put(String key, String value) {
        parameters.put(key, value);
    }

    public String remove(String key) {
        return parameters.remove(key);
    }
}
