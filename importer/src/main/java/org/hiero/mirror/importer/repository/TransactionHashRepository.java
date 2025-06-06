// SPDX-License-Identifier: Apache-2.0

package org.hiero.mirror.importer.repository;

import com.hedera.mirror.common.domain.transaction.TransactionHash;
import org.springframework.data.repository.CrudRepository;

public interface TransactionHashRepository extends CrudRepository<TransactionHash, byte[]> {}
