package i5.las2peer.services.socialBotManagerService.parser.openapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;

public class OpenAPIConnectorTest {

	@Test
	public void ReadFunctionV3Test() {

		ServiceFunction action = new ServiceFunction();
		action.setFunctionName("addPet");
		action.setServiceName("https://petstore3.swagger.io");
		ServiceFunction result = OpenAPIConnector.readFunction(action);

		assertEquals("post", result.getHttpMethod());
		assertNotNull(result.getAttributes());

		assertEquals("Add a new pet to the store", result.getFunctionDescription());

		assertEquals(1, result.getAttributes().size());
		Iterator<ServiceFunctionAttribute> iter = result.getAttributes().iterator();

		// pet object
		ServiceFunctionAttribute pet = iter.next();
		assertEquals(ParameterType.BODY, pet.getParameterType());
		iter = pet.getChildAttributes().iterator();

		// pet id
		ServiceFunctionAttribute petId = iter.next();
		assertEquals("10", petId.getExample());
		assertEquals("integer", petId.getContentType());
		assertEquals(ParameterType.CHILD, petId.getParameterType());
		assertFalse(petId.isRequired());
		assertFalse(petId.isArray());

		// pet name
		ServiceFunctionAttribute petName = iter.next();
		assertEquals("doggie", petName.getExample());
		assertEquals("string", petName.getContentType());
		assertEquals(ParameterType.CHILD, petName.getParameterType());
		assertTrue(petName.isRequired());
		assertFalse(petName.isArray());

		// category
		ServiceFunctionAttribute category = iter.next();
		assertEquals("object", category.getContentType());
		assertFalse(category.isRequired());
		assertNotNull(category.getChildAttributes());
		assertEquals(2, category.getChildAttributes().size());
		Iterator<ServiceFunctionAttribute> citer = category.getChildAttributes().iterator();

		// category id
		ServiceFunctionAttribute catId = citer.next();
		assertEquals("id", catId.getName());
		assertEquals("integer", catId.getContentType());
		assertFalse(catId.isRequired());

		// category name
		ServiceFunctionAttribute caname = citer.next();
		assertEquals("name", caname.getName());
		assertEquals("string", caname.getContentType());
		assertFalse(caname.isRequired());

		// photoUrls
		ServiceFunctionAttribute photoUrls = iter.next();
		assertEquals("photoUrls", photoUrls.getName());
		assertEquals("string", photoUrls.getContentType());
		assertTrue(photoUrls.isArray());

		// tags
		ServiceFunctionAttribute tags = iter.next();
		assertTrue(tags.isArray());
		assertFalse(tags.isRequired());

		// status
		ServiceFunctionAttribute status = iter.next();
		assertEquals("status", status.getName());
		assertEquals("pet status in the store", status.getDescription());
		assertFalse(status.isRequired());
		assertFalse(status.isArray());
		assertEquals(ParameterType.CHILD, status.getParameterType());
		assertEquals("enum", status.getContentType());
		assertNotNull(status.getEnumList());
		List<String> enumList = status.getEnumList();
		assertEquals(3, enumList.size());
		assertEquals("available", enumList.get(0));
		assertEquals("pending", enumList.get(1));
		assertEquals("sold", enumList.get(2));

	}

	@Test
	public void ReadFunctionV2Test() {

		ServiceFunction action = new ServiceFunction();
		action.setFunctionName("addPet");
		action.setServiceName("https://petstore.swagger.io");
		ServiceFunction result = OpenAPIConnector.readFunction(action);

		assertEquals("post", result.getHttpMethod());
		assertNotNull(result.getAttributes());
		assertEquals("Add a new pet to the store", result.getFunctionDescription());

		assertEquals(1, result.getAttributes().size());
		Iterator<ServiceFunctionAttribute> iter = result.getAttributes().iterator();

		System.out.println(result);
		// pet object
		ServiceFunctionAttribute pet = iter.next();
		assertEquals("Pet", pet.getName());
		assertEquals(ParameterType.BODY, pet.getParameterType());
		iter = pet.getChildAttributes().iterator();

		// pet id
		ServiceFunctionAttribute petId = iter.next();
		assertEquals("id", petId.getName());
		assertEquals("integer", petId.getContentType());
		assertEquals(ParameterType.CHILD, petId.getParameterType());
		assertFalse(petId.isRequired());
		assertFalse(petId.isArray());

		// category
		ServiceFunctionAttribute category = iter.next();
		assertEquals("object", category.getContentType());
		assertFalse(category.isRequired());
		assertNotNull(category.getChildAttributes());
		assertEquals(2, category.getChildAttributes().size());
		Iterator<ServiceFunctionAttribute> citer = category.getChildAttributes().iterator();

		// category id
		ServiceFunctionAttribute catId = citer.next();
		assertEquals("id", catId.getName());
		assertEquals("integer", catId.getContentType());
		assertFalse(catId.isRequired());

		// category name
		ServiceFunctionAttribute caname = citer.next();
		assertEquals("name", caname.getName());
		assertEquals("string", caname.getContentType());
		assertFalse(caname.isRequired());

		// pet name
		ServiceFunctionAttribute petName = iter.next();
		assertEquals("doggie", petName.getExample());
		assertEquals("string", petName.getContentType());
		assertEquals(ParameterType.CHILD, petName.getParameterType());
		assertTrue(petName.isRequired());
		assertFalse(petName.isArray());

		// photoUrls
		ServiceFunctionAttribute photoUrls = iter.next();
		assertEquals("photoUrls", photoUrls.getName());
		assertEquals("string", photoUrls.getContentType());
		assertTrue(photoUrls.isArray());
		assertTrue(photoUrls.isRequired());

		// tags
		ServiceFunctionAttribute tags = iter.next();
		assertTrue(tags.isArray());
		assertFalse(tags.isRequired());

		// status
		ServiceFunctionAttribute status = iter.next();
		assertEquals("status", status.getName());
		assertEquals("pet status in the store", status.getDescription());
		assertFalse(status.isRequired());
		assertFalse(status.isArray());
		assertEquals(ParameterType.CHILD, status.getParameterType());
		assertEquals("enum", status.getContentType());
		assertNotNull(status.getEnumList());
		List<String> enumList = status.getEnumList();
		assertEquals(3, enumList.size());
		assertEquals("available", enumList.get(0));
		assertEquals("pending", enumList.get(1));
		assertEquals("sold", enumList.get(2));

	}

}
