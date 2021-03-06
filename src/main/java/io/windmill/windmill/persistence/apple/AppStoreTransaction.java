//
//  Created by Markos Charatzas (markos@qnoid.com)
//  Copyright © 2014-2020 qnoid.com. All rights reserved.
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  Permission is granted to anyone to use this software for any purpose,
//  including commercial applications, and to alter it and redistribute it
//  freely, subject to the following restrictions:
//
//  This software is provided 'as-is', without any express or implied
//  warranty.  In no event will the authors be held liable for any damages
//  arising from the use of this software.
//
//  1. The origin of this software must not be misrepresented; you must not
//     claim that you wrote the original software. If you use this software
//     in a product, an acknowledgment in the product documentation is required.
//  2. Altered source versions must be plainly marked as such, and must not be
//     misrepresented as being the original software.
//  3. This notice may not be removed or altered from any source distribution.

package io.windmill.windmill.persistence.apple;

import java.time.Instant;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.windmill.windmill.persistence.Subscription;
import io.windmill.windmill.web.JsonbAdapterInstantToEpochSecond;

@Entity
@Table(schema="apple",name="transaction")
@NamedQuery(name = "transaction.find_by_identifier", query = "SELECT t FROM AppStoreTransaction t WHERE t.identifier = :identifier")
public class AppStoreTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    @NotNull
    private String identifier;

    @NotNull
    private String receipt;

    @Column(name="expires_at")
    @NotNull
    private Instant expiresAt;
    
    @Column(name="created_at")
    @NotNull
    private Instant createdAt;

    @Column(name="modified_at")
    private Instant modifiedAt;

    @ManyToOne
    AppStoreTransaction parent;
    
    @OneToOne(cascade = CascadeType.PERSIST, fetch=FetchType.LAZY)   
    /* This is only applicable in a test environment which auto generates the schema.
     * For some reason Hibernate (5.4.1.Final) logs an error message when trying to create a foreign key. 
     * The mvn test action completes succesfully, still the error detracts the value of the log.   
     */    
    @JoinColumn(foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @NotNull
    Subscription subscription;

    /**
     * 
     */
    public AppStoreTransaction()
    {
		this.receipt = "";
		this.expiresAt = Instant.now();
		this.subscription = new Subscription();
		this.subscription.setTransaction(this);
        this.createdAt = Instant.now();		
    }
    
    public AppStoreTransaction(String identifier, String receipt, Instant expiresAt, Subscription subscription) {
		super();
		this.identifier = identifier;
		this.receipt = receipt;
		this.expiresAt = expiresAt;
		this.subscription = subscription;
		this.subscription.setTransaction(this);
        this.createdAt = Instant.now();		
	}



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
       
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}	
	
	public String getReceipt() {
		return receipt;
	}

	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}

	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	@JsonbTypeAdapter(JsonbAdapterInstantToEpochSecond.class)
	public Instant getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Instant modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public AppStoreTransaction getParent() {
		return parent;
	}

	public void setParent(AppStoreTransaction parent) {
		this.parent = parent;
	}

	public Subscription getSubscription() {
		return subscription;
	}

	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.setTransaction(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		
		if (!(that instanceof AppStoreTransaction))
			return false;
		
		AppStoreTransaction transaction = (AppStoreTransaction) that;
		
		return this.identifier.equals(transaction.identifier);
	}

	public boolean hasExpired() {
		return this.expiresAt.isBefore(Instant.now());
	}
}
