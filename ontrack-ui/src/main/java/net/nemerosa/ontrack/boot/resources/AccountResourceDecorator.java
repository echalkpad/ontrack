package net.nemerosa.ontrack.boot.resources;

import net.nemerosa.ontrack.boot.ui.AccountController;
import net.nemerosa.ontrack.model.security.Account;
import net.nemerosa.ontrack.ui.resource.AbstractResourceDecorator;
import net.nemerosa.ontrack.ui.resource.Link;
import net.nemerosa.ontrack.ui.resource.ResourceContext;

import java.util.List;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

public class AccountResourceDecorator extends AbstractResourceDecorator<Account> {

    public AccountResourceDecorator() {
        super(Account.class);
    }

    @Override
    public List<Link> links(Account account, ResourceContext resourceContext) {
        return resourceContext.links()
                // Self
                .self(on(AccountController.class).getAccount(account.getId()))
                        // TODO Update
                        // TODO Delete
                        // OK
                .build();
    }
}
