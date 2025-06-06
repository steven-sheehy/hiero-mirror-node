// SPDX-License-Identifier: Apache-2.0

package org.hiero.mirror.importer.parser.record.transactionhandler;

import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.token.TokenAccount;
import com.hedera.mirror.common.domain.token.TokenKycStatusEnum;
import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.common.domain.transaction.Transaction;
import com.hedera.mirror.common.domain.transaction.TransactionType;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import org.hiero.mirror.importer.parser.record.entity.EntityListener;
import org.hiero.mirror.importer.parser.record.entity.EntityProperties;

@Named
@RequiredArgsConstructor
class TokenGrantKycTransactionHandler extends AbstractTransactionHandler {

    private final EntityListener entityListener;
    private final EntityProperties entityProperties;

    @Override
    public EntityId getEntity(RecordItem recordItem) {
        return EntityId.of(recordItem.getTransactionBody().getTokenGrantKyc().getAccount());
    }

    @Override
    public TransactionType getType() {
        return TransactionType.TOKENGRANTKYC;
    }

    @Override
    protected void doUpdateTransaction(Transaction transaction, RecordItem recordItem) {
        if (!entityProperties.getPersist().isTokens() || !recordItem.isSuccessful()) {
            return;
        }

        var transactionBody = recordItem.getTransactionBody().getTokenGrantKyc();
        var tokenId = EntityId.of(transactionBody.getToken());

        var tokenAccount = new TokenAccount();
        tokenAccount.setAccountId(transaction.getEntityId().getId());
        tokenAccount.setAssociated(true);
        tokenAccount.setKycStatus(TokenKycStatusEnum.GRANTED);
        tokenAccount.setTimestampLower(recordItem.getConsensusTimestamp());
        tokenAccount.setTokenId(tokenId.getId());
        entityListener.onTokenAccount(tokenAccount);
        recordItem.addEntityId(tokenId);
    }
}
