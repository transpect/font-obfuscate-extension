package org.letex.calabash.extensions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.QName;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.model.RuntimeValue;

public class FontObfuscate extends DefaultStep {
    private WritablePipe result = null;

    public FontObfuscate(XProcRuntime runtime, XAtomicStep step) {
	super(runtime,step);
    }
        @Override
	public void setOutput(String port, WritablePipe pipe) {
	    result = pipe;
	}
        @Override
	public void reset() {
	    result.resetWriter();
	}
        @Override
	public void run() throws SaxonApiException {
	    super.run();

	    RuntimeValue file = getOption(new QName("file"));
	    RuntimeValue uid  = getOption(new QName("uid"));

	    // submit empty string if attribute is not set
	    String fileString = (file != null) ? file.getString() : "";
	    String uidString  = (uid  != null) ? uid.getString()  : "";
	    
	    try{
		InputStream in = new FileInputStream(fileString);		

		String obfuscationKey = makeObfuscationKey(uidString);
		try(OutputStream out = new FileOutputStream(fileString + ".tmp")){
		    serialize(in, out, obfuscationKey);
		}catch (IOException ioex) {

		}
	    } catch (FileNotFoundException fex){
		fex.printStackTrace();
	    }
	}

    /** credits to Eliot Kimber and Adobe, code below was taken from these sources:
      *
      * http://blogs.adobe.com/digitaleditions/2009/05/font_mangling_code_available_f.html 
      * https://github.com/dita4publishers/epub-font-obfuscator/
      */
    
    /** Implements the Obfuscation Algorithm from 
      * http://www.openebook.org/doc_library/informationaldocs/FontManglingSpec.html
      * 
      */
    
    public static void serialize(InputStream in, OutputStream out, String obfuscationKey) throws IOException
    {
	byte[] mask = makeXORMask(obfuscationKey);
	try {
	    byte[] buffer = new byte[4096];
	    int len;
	    boolean first = true;
	    while ((len = in.read(buffer)) > 0) {
		if( first && mask != null ) {
		    first = false;
		    for( int i = 0 ; i < 1040 ; i++ ) {
			buffer[i] = (byte)(buffer[i] ^ mask[i % mask.length]);
		    }
		}
		out.write(buffer, 0, len);
	    }
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
	out.close();
    }
    
    /** Every EPUB needs a unique identifier, this could be an ISBN or other identifier.
      * In this case we're generating a random identifier.
      *
      * For the purposes of font obfuscation, this does not need to be random, just unique (like an ISBN)
      */
 
    public static String makeObfuscationKey(String... UIDs) {
	StringBuilder buf = new StringBuilder();
	String sep = "";
	for (String uid : UIDs) {
	    String keyPart = uid.replaceAll("[\\s\\t\\n\\r]", "");
	    buf.append(sep).append(keyPart);
	    sep = " ";
	}
	return buf.toString();
    }

    private static byte[] makeXORMask(String opfUID) {
	if(opfUID == null) return null;

	ByteArrayOutputStream mask = new ByteArrayOutputStream();

	/** 
	 * This starts with the "unique-identifier", strips the whitespace, and applies SHA1 hash 
	 * giving a 20 byte key that we can apply to the font file.
	 * 
	 * See: http://www.idpf.org/epub/30/spec/epub30-ocf.html#fobfus-keygen
	 **/
	try {
	    Security.addProvider(
				 new com.sun.crypto.provider.SunJCE());
	    MessageDigest sha = MessageDigest.getInstance("SHA-1");
	    String temp = opfUID.trim();
	    sha.update(temp.getBytes("UTF-8"), 0, temp.length());mask.write(sha.digest());
	} catch (NoSuchAlgorithmException e) {
	    System.err.println("No such Algorithm (really, did I misspell SHA-1?");
	    System.err.println(e.toString());return null;
	} catch (IOException e) {
	    System.err.println("IO Exception. check out mask.write...");
	    System.err.println(e.toString());return null;
	}
	if (mask.size() != 20) {
	    System.err.println("makeXORMask should give 20 byte mask, but isn't");
	    return null;
	}
	return mask.toByteArray();
    }    
}

