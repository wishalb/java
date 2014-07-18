package com.adyen.examples.modifications;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;

import com.adyen.services.payment.ModificationRequest;
import com.adyen.services.payment.ModificationResult;
import com.adyen.services.payment.PaymentPortType;
import com.adyen.services.payment.PaymentService;
import com.adyen.services.payment.ServiceException;

/**
 * Cancel or Refund a Payment (SOAP)
 * 
 * If you do not know if the payment is captured but you want to reverse the authorisation you can send a modification
 * request to the cancelOrRefund action. This file shows how a payment can be cancelled or refunded by a modification
 * request using SOAP.
 * 
 * Please note: using our API requires a web service user. Set up your Webservice user:
 * Adyen CA >> Settings >> Users >> ws@Company. >> Generate Password >> Submit
 * 
 * @link /4.Modifications/Soap/CancelOrRefundPayment
 * @author Created by Adyen - Payments Made Easy
 */

@WebServlet("/4.Modifications/Soap/CancelOrRefundPayment")
public class CancelOrRefundPaymentSoap extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/**
		 * SOAP settings
		 * - wsdl: the WSDL url you are using (Test/Live)
		 * - wsUser: your web service user
		 * - wsPassword: your web service user's password
		 */
		String wsdl = "https://pal-test.adyen.com/pal/Payment.wsdl";
		String wsUser = "YourWSUser";
		String wsPassword = "YourWSPassword";

		/**
		 * Create SOAP client, using classes in adyen-wsdl-cxf.jar library (generated by wsdl2java tool, Apache CXF).
		 * 
		 * @see WebContent/WEB-INF/lib/adyen-wsdl-cxf.jar
		 */
		PaymentService service = new PaymentService(new URL(wsdl));
		PaymentPortType client = service.getPaymentHttpPort();

		// Set HTTP Authentication
		((BindingProvider) client).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, wsUser);
		((BindingProvider) client).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, wsPassword);

		/**
		 * Perform cancel or refund request by sending in a modificationRequest, the protocol is defined in the WSDL.
		 * The following parameters are used:
		 * 
		 * <pre>
		 * - merchantAccount        : The merchant account used to process the payment.
		 * - originalReference      : The pspReference that was assigned to the authorisation.
		 * - reference              : Your own reference or description of the modification. (optional)
		 * </pre>
		 */
		ModificationRequest modificationRequest = new ModificationRequest();
		modificationRequest.setMerchantAccount("YourMerchantAccount");
		modificationRequest.setOriginalReference("PspReferenceOfTheAuthorisedPayment");
		modificationRequest.setReference("YourReference");

		/**
		 * Send the cancel or refund request.
		 */
		ModificationResult modificationResult;
		try {
			modificationResult = client.cancelOrRefund(modificationRequest);
		} catch (ServiceException e) {
			throw new ServletException(e);
		}

		/**
		 * If the message was syntactically valid and merchantAccount is correct you will receive a modification
		 * response with the following fields:
		 * - pspReference: A new reference to uniquely identify this modification request.
		 * - response: A confirmation indicating we receievd the request: [cancelOrRefund-received].
		 * 
		 * If the payment is authorised, but not yet captured, it will be cancelled. In other cases the payment will be
		 * fully refunded (if possible).
		 * 
		 * Please note: The actual result of the cancel or refund is sent via a notification with eventCode
		 * CANCEL_OR_REFUND.
		 */
		PrintWriter out = response.getWriter();

		out.println("Modification Result:");
		out.println("- pspReference: " + modificationResult.getPspReference());
		out.println("- response: " + modificationResult.getResponse());
	}

}