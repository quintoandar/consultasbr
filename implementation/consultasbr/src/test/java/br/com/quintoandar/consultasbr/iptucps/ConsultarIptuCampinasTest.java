package br.com.quintoandar.consultasbr.iptucps;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.http.client.ClientProtocolException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.com.quintoandar.consultasbr.iptusp.ConsultarIptuCampinas;
import br.com.quintoandar.consultasbr.iptusp.ConsultarIptuCampinas.ConsultaBoletoResult;
import br.com.quintoandar.consultasbr.iptusp.IPTUSPException;
import br.com.quintoandar.consultasbr.iptusp.Resposta2ViaIPTU;
import br.com.quintoandar.consultasbr.pf.ConsultaAntecedentes;
import br.com.quintoandar.consultasbr.pf.ConsultarPF;
import br.com.quintoandar.consultasbr.pf.RespostaCaptcha;
import br.com.quintoandar.consultasbr.pf.ResultadoAntecedentes;
import br.com.quintoandar.consultasbr.pf.StatusAntecedentes;

public class ConsultarIptuCampinasTest {

	private static ConsultarIptuCampinas consultarIptuCampinas;
	
	@Before
	public void setBefore(){
		if(consultarIptuCampinas == null){
			consultarIptuCampinas = new ConsultarIptuCampinas();
		}
	}
	
	private String solveCaptcha(String name, byte[] captchaImage) {
		JLabel jLabel = new JLabel(new ImageIcon(captchaImage));
		jLabel.setText("Testing for: " + name);
		return JOptionPane.showInputDialog(jLabel);
	}

	@Test
	public void testAcessoConsultaIPTU() {
		
		RespostaCaptcha captcha = null;
		try {
			captcha = consultarIptuCampinas.requestCaptcha();
		} catch (IPTUSPException e) {
			Assert.fail("Exceção requisição de captcha " + e.getMessage());
		}
		
		String respCaptcha = solveCaptcha("IPTU Campinas", captcha.getCaptchaImage());

		try {
			ConsultaBoletoResult consultaBoletos = consultarIptuCampinas.consultaBoletos(captcha.getSessionId(), respCaptcha, "3414.32.29.0034.01056");
			Assert.assertNotNull(consultaBoletos);
			
			System.out.println("Iptu Campinas - teste acesso.");
			
		} catch (ClientProtocolException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		} catch (IPTUSPException e) {
			Assert.fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testConsultaIPTUCodCartograficoInvalido() {
		
		RespostaCaptcha captcha = null;
		try {
			captcha = consultarIptuCampinas.requestCaptcha();
		} catch (IPTUSPException e) {
			Assert.fail("Exceção requisição de captcha " + e.getMessage());
		}
		
		String respCaptcha = solveCaptcha("IPTU Campinas", captcha.getCaptchaImage());

		try {
			ConsultaBoletoResult consultaBoletos = consultarIptuCampinas.consultaBoletos(captcha.getSessionId(), respCaptcha, "3421.41.47.0073.010");
			System.out.println("Iptu Campinas - código cartográfico inválido.");
		} catch (ClientProtocolException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		} catch (IPTUSPException e) {
			Assert.assertTrue(e.getMessage().contains("Imóvel não encontrado"));
		}
		
	}
	
	@Test
	public void testConsultaIPTUNaoQuitado() {
		
		RespostaCaptcha captcha = null;
		try {
			captcha = consultarIptuCampinas.requestCaptcha();
		} catch (IPTUSPException e) {
			Assert.fail("Exceção requisição de captcha " + e.getMessage());
		}
		
		String respCaptcha = solveCaptcha("IPTU Campinas", captcha.getCaptchaImage());

		try {
			ConsultaBoletoResult consultaBoletos = consultarIptuCampinas.consultaBoletos(captcha.getSessionId(), respCaptcha, "3414.22.30.0025.02062");
			String retornoHtml = consultaBoletos.getRetornoHtml();
			Assert.assertNotNull(retornoHtml);
			
			FileOutputStream fos = new FileOutputStream(System.getProperty("user.home")+"/Documents/iptu-cps-devido.html");
			fos.write(retornoHtml.getBytes());
			fos.close();
			System.out.println("Iptu Campinas - quitado salvo.");
			
		} catch (ClientProtocolException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		} catch (IPTUSPException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testConsultaIPTUBoletos() {
		
		RespostaCaptcha captcha = null;
		try {
			captcha = consultarIptuCampinas.requestCaptcha();
		} catch (IPTUSPException e) {
			Assert.fail("Exceção requisição de captcha " + e.getMessage());
		}
		
		String respCaptcha = solveCaptcha("IPTU Campinas", captcha.getCaptchaImage());

		try {
			String codigoCartografico = "3441.34.11.0780.03066";
			ConsultaBoletoResult consultaBoletos = consultarIptuCampinas.consultaBoletos(captcha.getSessionId(), respCaptcha, codigoCartografico);
			List<Resposta2ViaIPTU> parcelas = consultarIptuCampinas.getParcelasIptu(consultaBoletos, codigoCartografico);
			Assert.assertNotNull(parcelas);
			
			for(Resposta2ViaIPTU p : parcelas){
				Assert.assertNotNull(p.getCodigo());
				Assert.assertNotNull(p.getCodigoContribuinte());
				Assert.assertNotNull(p.getAnoExercicio());
				Assert.assertNotNull(p.getNumParcela());
				Assert.assertNotNull(p.getValor());
				Assert.assertNotNull(p.getVencimento());
			}
			
		} catch (ClientProtocolException e) {
			Assert.fail(e.getMessage());
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		} catch (IPTUSPException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testConsultaIPTUCampinas() {
		
		RespostaCaptcha captcha = null;
		try {
			captcha = consultarIptuCampinas.requestCaptcha();
		} catch (IPTUSPException e) {
			Assert.fail("Exceção requisição de captcha " + e.getMessage());
		}
		
		String respCaptcha = solveCaptcha("IPTU Campinas", captcha.getCaptchaImage());

		try {
			String codigoCartografico = "3414.22.30.0025.02062";
			List<Resposta2ViaIPTU> parcelas = consultarIptuCampinas.buscar2aViaIPTU(captcha.getSessionId(), respCaptcha, codigoCartografico);
			Assert.assertNotNull(parcelas);
			
			for(Resposta2ViaIPTU p : parcelas){
				Assert.assertNotNull(p.getCodigo());
				Assert.assertNotNull(p.getCodigoContribuinte());
				Assert.assertNotNull(p.getAnoExercicio());
				Assert.assertNotNull(p.getNumParcela());
				Assert.assertNotNull(p.getValor());
				Assert.assertNotNull(p.getVencimento());
				
				System.out.println("Parcela " + p.getNumParcela() + " - " + p.getAnoExercicio());
				System.out.println("código contribuinte: " + p.getCodigoContribuinte());
				System.out.println("código: " + p.getCodigo());
				System.out.println("valor: R$" + p.getValor());
				System.out.println("vencimento: " + p.getVencimento());
				System.out.println("vencida: "  + (p.getIsVencida()?"Sim":"Não"));
			}
			
		} catch(Throwable t){
			Assert.fail("Exceção " + t.getMessage());
		}
	}

}
