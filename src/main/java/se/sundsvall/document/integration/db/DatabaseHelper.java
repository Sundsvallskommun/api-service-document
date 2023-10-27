package se.sundsvall.document.integration.db;

import static org.zalando.fauxpas.FauxPas.throwingFunction;

import java.io.IOException;
import java.sql.Blob;
import java.util.Optional;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;

@Component
public class DatabaseHelper {

	@Autowired
	private EntityManager entityManager;

	public Blob convertToBlob(MultipartFile multipartFile) {
		return Optional.ofNullable(multipartFile)
			.map(throwingFunction(this::createBlob))
			.orElse(null);
	}

	private Session getSession() {
		return entityManager.unwrap(Session.class);
	}

	private Blob createBlob(MultipartFile multipartFile) throws IOException {
		return getSession().getLobHelper().createBlob(multipartFile.getInputStream(), multipartFile.getSize());
	}
}
