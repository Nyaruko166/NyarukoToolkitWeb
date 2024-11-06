package me.nyaruko166.nyarukotoolkitweb.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Table(name = "Manga")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Manga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Host")
    private String host;

    @Column(name = "Title")
    private String title;

    @Column(name = "Cover")
    private String cover;

    @Column(name = "SourceURL")
    private String sourceURL;

    @Column(name = "DownloadURL")
    private String downloadURL;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "CreateDate")
    private Date createDate;

    @Column(name = "UpdateDate")
    private Date updateDate;

    @Column(name = "AccountId")
    private Integer accountId;
}
