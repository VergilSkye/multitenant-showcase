package hr.ivlahek.showcase.aop;

import hr.ivlahek.showcase.persistence.repository.TenantRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Aspect
@Component
public class RepositoryInterceptor {

    @Autowired
    private EntityManager entityManager;

    private Logger logger = LoggerFactory.getLogger(RepositoryInterceptor.class);

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Around("execution(* org.springframework.data.repository.Repository+.*(..))")
    public Object inWebLayer(ProceedingJoinPoint joinPoint) throws Throwable {

        if (activeProfile.equals("prod") ) {
            if (entityManager.isOpen() && notOfAClassTypeTenantRepository(joinPoint.getSignature().getDeclaringType())) {
                Session session = entityManager.unwrap(Session.class);
                Integer id = TenantContext.getCurrentTenantId();
                session.enableFilter("userAccountFilter").setParameter("tenantId", id);
                session.enableFilter("mobileApplicationFilter").setParameter("tenantId", id);
            }
        } else {
            if (entityManager.isJoinedToTransaction() && notOfAClassTypeTenantRepository(joinPoint.getSignature().getDeclaringType())) {
                Session session = entityManager.unwrap(Session.class);
                Integer id = TenantContext.getCurrentTenantId();
                session.enableFilter("userAccountFilter").setParameter("tenantId", id);
                session.enableFilter("mobileApplicationFilter").setParameter("tenantId", id);
            }
        }
        return joinPoint.proceed();
    }

    private boolean notOfAClassTypeTenantRepository(Class declaringType) {
        return !declaringType.equals(TenantRepository.class);
    }

}
