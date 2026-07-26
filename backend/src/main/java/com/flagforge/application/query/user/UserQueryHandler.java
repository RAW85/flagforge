package com.flagforge.application.query.user;

import com.flagforge.domain.repository.UserRepository;
import com.flagforge.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** ADMIN user listing. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryHandler {

	private final UserRepository userRepository;

	public List<User> handle(ListUsersQuery query) {
		return userRepository.findAllByOrderByCreatedAtDesc();
	}
}
