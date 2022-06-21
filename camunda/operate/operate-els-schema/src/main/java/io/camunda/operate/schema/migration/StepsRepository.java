package io.camunda.operate.schema.migration;

import io.camunda.operate.exceptions.MigrationException;
import java.io.IOException;
import java.util.List;

public interface StepsRepository {
   void updateSteps() throws IOException, MigrationException;

   void save(Step var1) throws MigrationException, IOException;

   List findAll() throws IOException;

   List findNotAppliedFor(String var1) throws IOException;

   String getName();
}
