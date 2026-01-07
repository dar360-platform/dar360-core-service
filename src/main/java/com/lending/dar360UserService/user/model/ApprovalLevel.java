
package com.lending.dar360UserService.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Entity
@Table(name = "[approval_level]")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@Slf4j
public class ApprovalLevel implements Serializable {

    private static final long serialVersionUID = -6271388606411349270L;
    @Id
    @Column(name = "id")
    @Schema(description = "Id of approval level", example = "0013db76-92c6-466f-84da-ee4e83ffcd90")
    private String id;
    @Column(name = "name")
    @Schema(description = "Name of approval level", example = "abc")
    private String name;
}
