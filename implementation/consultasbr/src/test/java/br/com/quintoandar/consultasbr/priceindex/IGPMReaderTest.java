package br.com.quintoandar.consultasbr.priceindex;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;

public class IGPMReaderTest {

	@Test
	public void testProcessarIGPM() throws Throwable {
		IGPMReader r = new IGPMReader();
		InputStream is = IGPMReader.class.getResourceAsStream("/igpm.txt");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int bufSize = 0; 
		while((bufSize = is.available()) > 0){
			byte[] buf = new byte[bufSize < 1024?bufSize:1024];
			is.read(buf);
			baos.write(buf);
		}
		is.close();
		baos.close();
		
		r.processar(new String(baos.toByteArray(),"utf8"));

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		assertEquals(sdf.parse("01/02/2015"),r.getMes());
		assertEquals(new BigDecimal("3.8499"), r.getAcumulado());
		assertEquals(new BigDecimal("0.27"), r.getMensal());
	}

}
