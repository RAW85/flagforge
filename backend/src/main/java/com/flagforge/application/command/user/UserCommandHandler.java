package com.flagforge.application.command.user;

import com.flagforge.domain.audit.AuditAction;
import com.flagforge.domain.audit.AuditEvent;
import com.flagforge.domain.exception.BusinessRuleException;
import com.flagforge.domain.exception.ResourceNotFoundException;
import com.flagforge.domain.repository.AuditEventRepository;
import com.flagforge.domain.repository.UserRepository;
import com.flagforge.domain.user.User;
import com.flagforge.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin mutations for users. Guards: cannot demote/disable the last enabled ADMIN;
 * actors cannot disable themselves.
 */
@Service
@RequiredArgsConstructor
public class UserCommandHandler {

	private final UserRepository userRepository;
	private final AuditEventRepository auditEventRepository;

	@Transactional
	public User handle(UpdateUserRoleCommand command) {
		if (command.role() == null) {
			throw new BusinessRuleException("Role is required");
		}

		User user = requireUser(command.userId());
		UserRole previous = user.getRole();

		if (previous == command.role()) {
			return user;
		}

		// Prevent removing the last enabled admin
		if (previous == UserRole.ADMIN
				&& command.role() != UserRole.ADMIN
				&& user.isEnabled()
				&& userRepository.countByRoleAndEnabledTrue(UserRole.ADMIN) <= 1) {
			throw new BusinessRuleException("Cannot demote the last enabled ADMIN");
		}

		user.setRole(command.role());
		User saved = userRepository.save(user);

		auditEventRepository.save(AuditEvent.builder()
				.entityType("User")
				.entityId(saved.getId())
				.action(AuditAction.UPDATE)
				.actorId(command.actorId())
				.detailsJson("{\"field\":\"role\",\"from\":\"" + previous + "\",\"to\":\"" + command.role() + "\"}")
				.build());

		return saved;
	}

	@Transactional
	public User handle(SetUserEnabledCommand command) {
		User user = requireUser(command.userId());

		if (user.getId().equals(command.actorId()) && !command.enabled()) {
			throw new BusinessRuleException("You cannot disable your own account");
		}

		if (user.isEnabled() == command.enabled()) {
			return user;
		}

		if (user.getRole() == UserRole.ADMIN
				&& user.isEnabled()
				&& !command.enabled()
				&& userRepository.countByRoleAndEnabledTrue(UserRole.ADMIN) <= 1) {
			throw new BusinessRuleException("Cannot disable the last enabled ADMIN");
		}

		user.setEnabled(command.enabled());
		User saved = userRepository.save(user);

		auditEventRepository.save(AuditEvent.builder()
				.entityType("User")
				.entityId(saved.getId())
				.action(command.enabled() ? AuditAction.ENABLE : AuditAction.DISABLE)
				.actorId(command.actorId())
				.detailsJson("{\"enabled\":" + command.enabled() + "}")
				.build());

		return saved;
	}

	private User requireUser(java.util.UUID id) {
		return userRepository.findById(id)
				.orElseThrow(() -> ResourceNotFoundException.of("User", id));
	}
}
