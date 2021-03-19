package br.com.quintoandar.consultasbr.crecisp;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

@Ignore("Analyze if these tests should still exist")
public class ConsultarCreciTest {
	
	ConsultarCreci consultarCreci;
	
	@Before
	public void setUp(){
		consultarCreci = new ConsultarCreci();
	}

	@Test
	public void testSimpleQuery() {
		ResultadoCreci resCreci = consultarCreci.consultar("137.304", TipoCreci.Fisica);
		
		assertNotNull(resCreci);
		assertNotNull(resCreci.getHtml());
		assertNotEquals(resCreci.getHtml().length,0);
		System.out.println(resCreci.getCreci()+" "+resCreci.getTipo()+" "+resCreci.getStatus());
	}

}
