package com.flozano.s3mavenproxy.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.HandlerMapping;

import com.flozano.s3mavenproxy.domain.Artifact;
import com.flozano.s3mavenproxy.domain.ForbiddenException;
import com.flozano.s3mavenproxy.domain.MavenRepositoryBackend;
import com.flozano.s3mavenproxy.domain.NotFoundException;

@Controller
public class MavenRepositoryController {

	@Autowired
	@Qualifier("backend")
	MavenRepositoryBackend backend;

	private static Artifact getArtifact(HttpServletRequest request) {
		String path = (String) request
				.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		return Artifact.fromPath(path);
	}

	@RequestMapping(value = "/**", method = RequestMethod.GET, produces = "text/plain")
	public @ResponseBody DeferredResult<ResponseEntity<String>> get(
			HttpServletRequest request) {

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

	@RequestMapping(value = "/**", method = RequestMethod.PUT, produces = "text/plain", consumes = "*/*")
	public @ResponseBody DeferredResult<ResponseEntity<String>> put(
			HttpServletRequest request) throws IOException {
		DeferredResult<ResponseEntity<String>> deferred = new DeferredResult<>();
		backend.put(getArtifact(request), request.getContentType(),
				request.getContentLengthLong(), request.getInputStream())
				.handle((res, error) -> {
					if (error != null) {
						deferred.setErrorResult(error);
					} else {
						deferred.setResult(ResponseEntity
								.ok("Artifact uploaded"));
					}
					return null;
				});
		return deferred;
	}

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public @ResponseBody ResponseEntity<?> handleNotFound(NotFoundException e) {
		return ResponseEntity.notFound().build();
	}

	@ExceptionHandler(ForbiddenException.class)
	public @ResponseBody ResponseEntity<?> handleForbidden(ForbiddenException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}
}
