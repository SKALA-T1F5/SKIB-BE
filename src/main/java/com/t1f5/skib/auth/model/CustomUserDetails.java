package com.t1f5.skib.auth.model;

import com.t1f5.skib.admin.model.Admin;
import com.t1f5.skib.user.model.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {
  private final String identifier; // email or id
  private final String password;
  private final String role;

  public CustomUserDetails(String identifier, String password, String role) {
    this.identifier = identifier;
    this.password = password;
    this.role = role;
  }

  public static CustomUserDetails fromUser(User user) {
    return new CustomUserDetails(user.getEmail(), user.getPassword(), user.getType().name());
  }

  public static CustomUserDetails fromAdmin(Admin admin) {
    return new CustomUserDetails(admin.getId().toString(), admin.getPassword(), "ADMIN");
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role)); // 예: ROLE_ADMIN
  }

  @Override
  public String getUsername() {
    return identifier;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
