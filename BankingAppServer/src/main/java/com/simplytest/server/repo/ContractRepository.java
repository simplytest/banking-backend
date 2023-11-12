package com.simplytest.server.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.simplytest.server.model.DBContract;

@Repository
public interface ContractRepository extends JpaRepository<DBContract, Long>
{
}