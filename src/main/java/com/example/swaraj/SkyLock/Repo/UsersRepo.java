package com.example.swaraj.SkyLock.Repo;

import com.example.swaraj.SkyLock.Models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepo extends JpaRepository<Users,Integer> {

    Users findByUsername(String username);
}
