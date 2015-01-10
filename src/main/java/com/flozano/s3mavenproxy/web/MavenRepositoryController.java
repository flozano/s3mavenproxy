package com.flozano.s3mavenproxy.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.HandlerMapping;

import com.flozano.s3mavenproxy.domain.Artifact;
import com.flozano.s3mavenproxy.domain.ForbiddenException;
import com.flozano.s3mavenproxy.domain.MavenRepositoryBackend;
import com.flozano.s3mavenproxy.domain.NotFoundException;

@RestController
public class MavenRepositoryController {

	@Autowired
	@Qualifier("backend")
	MavenRepositoryBackend backend;

	private static Artifact getArtifact(HttpServletRequest request) {
		String path = (String) request
				.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		return Artifact.fromPath(path);
	}

	@RequestMapping(value = "/**", method = RequestMethod.GET)
	public DeferredResult<ResponseEntity<String>> get(HttpServletRequest request) {

		DeferredResult<ResponseEntity<String>> deferred = new DeferredResult<>();

		backend.get(getArtifact(request)).handle(
				(res, error) -> {
					if (error != null) {
						deferred.setErrorResult(error);
					} else {
						;
						deferred.setResult(ResponseEntity
								.status(HttpStatus.FOUND)
								.header("Location",
										res.getTargetURI().toString())
								.body("Artifact found in "
										+ res.getTargetURI().toString()));
					}
					return null;
				});
		return deferred;
	}

	@RequestMapping(value = "/**", method = RequestMethod.PUT)
	public DeferredResult<ResponseEntity<String>> put(HttpServletRequest request)
			throws IOException {
		Artifact artifact = getArtifact(request);

		DeferredResult<ResponseEntity<String>> deferred = new DeferredResult<>();
		backend.put(artifact, request.getContentType(),
				request.getContentLengthLong(), request.getInputStream())
				.handle((res, error) -> {
					if (error != null) {
						deferred.setErrorResult(error);
					} else {
						try {
							deferred.setResult(ResponseEntity.created(
									getURIForArtifact(artifact)).body(
									"Item uploaded"));
						} catch (Exception e) {
							deferred.setErrorResult(e);
						}
					}
					return null;
				});
		return deferred;
	}

	private URI getURIForArtifact(Artifact artifact) throws URISyntaxException {
		return new URI(artifact.getPath());
	}

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public ResponseEntity<?> handleNotFound(NotFoundException e) {
		return ResponseEntity.notFound().build();
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<?> handleForbidden(ForbiddenException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}

	@ExceptionHandler({ IllegalArgumentException.class })
	public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
		return ResponseEntity.badRequest().build();
	}
}
