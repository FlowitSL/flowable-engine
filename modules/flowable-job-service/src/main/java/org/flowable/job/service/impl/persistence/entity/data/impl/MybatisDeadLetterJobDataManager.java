/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.job.service.impl.persistence.entity.data.impl;

import java.util.HashMap;
import java.util.List;

import org.flowable.engine.common.impl.db.AbstractDataManager;
import org.flowable.engine.common.impl.db.CachedEntityMatcher;
import org.flowable.job.api.Job;
import org.flowable.job.service.impl.DeadLetterJobQueryImpl;
import org.flowable.job.service.impl.persistence.entity.DeadLetterJobEntity;
import org.flowable.job.service.impl.persistence.entity.DeadLetterJobEntityImpl;
import org.flowable.job.service.impl.persistence.entity.data.DeadLetterJobDataManager;
import org.flowable.job.service.impl.persistence.entity.data.impl.cachematcher.DeadLetterJobsByExecutionIdMatcher;

/**
 * @author Tijs Rademakers
 */
public class MybatisDeadLetterJobDataManager extends AbstractDataManager<DeadLetterJobEntity> implements DeadLetterJobDataManager {

    protected CachedEntityMatcher<DeadLetterJobEntity> deadLetterByExecutionIdMatcher = new DeadLetterJobsByExecutionIdMatcher();

    @Override
    public Class<? extends DeadLetterJobEntity> getManagedEntityClass() {
        return DeadLetterJobEntityImpl.class;
    }

    @Override
    public DeadLetterJobEntity create() {
        return new DeadLetterJobEntityImpl();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Job> findJobsByQueryCriteria(DeadLetterJobQueryImpl jobQuery) {
        String query = "selectDeadLetterJobByQueryCriteria";
        return getDbSqlSession().selectList(query, jobQuery);
    }

    @Override
    public long findJobCountByQueryCriteria(DeadLetterJobQueryImpl jobQuery) {
        return (Long) getDbSqlSession().selectOne("selectDeadLetterJobCountByQueryCriteria", jobQuery);
    }

    @Override
    public List<DeadLetterJobEntity> findJobsByExecutionId(String executionId) {
        return getList("selectDeadLetterJobsByExecutionId", executionId, deadLetterByExecutionIdMatcher, true);
    }

    @Override
    public void updateJobTenantIdForDeployment(String deploymentId, String newTenantId) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("deploymentId", deploymentId);
        params.put("tenantId", newTenantId);
        getDbSqlSession().update("updateDeadLetterJobTenantIdForDeployment", params);
    }

}
