package br.com.quintoandar.consultasbr.iptusp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConsultaDebitoIptuTest {

private static ConsultarIptuSP consultarIptuSP;
	
	@Before
	public void setBefore(){
		if(consultarIptuSP == null){
			consultarIptuSP = new ConsultarIptuSP();
		}
	}
	
	private String readResourceFileAsString(String filename) throws IOException{
		ClassLoader classLoader = getClass().getClassLoader();
		byte[] encoded = Files.readAllBytes(Paths.get(classLoader.getResource(filename).getPath()));
		return new String(encoded, StandardCharsets.ISO_8859_1);
		
	}
	
	/*
	 * arquivo: iptu_teste_1.html
	 * ano exercicio: 2016
	 * contribuinte: 016.065.0746-1
	 * total atual: 448,38
	 * parcelas vencidas atual: ()
	 * parcelas abertas atual: (8 9 10)
	 * 
	 * Dividas anteriores : nao há
	 */
	@Test
	public void testConsulta1() throws IOException{
		String strFile = readResourceFileAsString("iptu_teste_1.html");		
		RespostaConsultaDebito resposta = consultarIptuSP.gerarRespostaConsultaDebito("016.065.0746-1",strFile, consultarIptuSP.parseDocument(strFile), 2016);
		
		Assert.assertEquals("016.065.0746-1", resposta.getCodigoContribuinte());
		Assert.assertEquals(new Integer(2016), resposta.getAnoExercicioAtual());
		Assert.assertTrue(resposta.getPossuiDebitos());
		
		
		Assert.assertTrue(resposta.getDebitoExercicioAtual() != null);
		Assert.assertTrue(resposta.getDebitoExercicioAtual().getParcelasEmAberto() != null);
		Assert.assertTrue(resposta.getDebitoExercicioAtual().getParcelasVencidas() != null);
		Assert.assertEquals(new Double(448.38), resposta.getDebitoExercicioAtual().getTotalDebito());
		Assert.assertTrue(resposta.getDebitoExercicioAtual().getParcelasVencidas().size() == 0);

		Set<Integer> expected = new HashSet<Integer>(Arrays.asList(8, 9, 10));
		Assert.assertEquals(expected, resposta.getDebitoExercicioAtual().getParcelasEmAberto());
		
		Assert.assertTrue(resposta.getDebitosAnteriores().size() == 0);
	}
	
	/*
	 * arquivo: iptu_teste_2.html
	 * ano exercicio: 2016
	 * contribuinte: 047.035.0409-1
	 * total atual: 1.293,53
	 * parcelas vencidas atual: (1 2 3 4)
	 * parcelas abertas atual: (1 2 3 4 5 6 7 8 9 10)
	 * 
	 * Dividas anteriores : 
	 * 2015 (1 2 3 4 5 6 7 8 9 10)
	 */
	@Test
	public void testConsulta2() throws IOException{
		String strFile = readResourceFileAsString("iptu_teste_2.html");
		RespostaConsultaDebito resposta = consultarIptuSP.gerarRespostaConsultaDebito("047.035.0409-1",strFile, consultarIptuSP.parseDocument(strFile), 2016);
		
		Assert.assertEquals("047.035.0409-1", resposta.getCodigoContribuinte());
		Assert.assertEquals(new Integer(2016), resposta.getAnoExercicioAtual());
		Assert.assertTrue(resposta.getPossuiDebitos());
		
		
		Assert.assertTrue(resposta.getDebitoExercicioAtual() != null);
		Assert.assertTrue(resposta.getDebitoExercicioAtual().getParcelasEmAberto() != null);
		Assert.assertTrue(resposta.getDebitoExercicioAtual().getParcelasVencidas() != null);
		Assert.assertEquals(new Double(1293.53), resposta.getDebitoExercicioAtual().getTotalDebito());
		
		Set<Integer> expected = new HashSet<Integer>(Arrays.asList(1, 2, 3, 4));
		Assert.assertEquals(expected, resposta.getDebitoExercicioAtual().getParcelasVencidas());

		expected = new HashSet<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		Assert.assertEquals(expected, resposta.getDebitoExercicioAtual().getParcelasEmAberto());
				
		Assert.assertTrue(resposta.getDebitosAnteriores().size() == 1);
		Assert.assertEquals(expected, resposta.getDebitosAnteriores().get(2015).getParcelasEmAberto());
		Assert.assertEquals(expected, resposta.getDebitosAnteriores().get(2015).getParcelasVencidas());
	}
	
	/*
	 * arquivo: iptu_teste_3.html
	 * ano exercicio: 2016
	 * Nenhuma divida
	 */
	@Test
	public void testConsulta3() throws IOException{
		String strFile = readResourceFileAsString("iptu_teste_3.html");
		RespostaConsultaDebito resposta = consultarIptuSP.gerarRespostaConsultaDebito("011.150.0088-8",strFile, consultarIptuSP.parseDocument(strFile), 2016);
		
		Assert.assertEquals("011.150.0088-8", resposta.getCodigoContribuinte());
		Assert.assertEquals(new Integer(2016), resposta.getAnoExercicioAtual());
		Assert.assertFalse(resposta.getPossuiDebitos());		
		
		Assert.assertTrue(resposta.getDebitoExercicioAtual() == null);
						
		Assert.assertTrue(resposta.getDebitosAnteriores().size() == 0);
	}
	
}
