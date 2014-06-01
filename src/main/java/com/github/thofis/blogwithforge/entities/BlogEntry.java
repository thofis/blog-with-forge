package com.github.thofis.blogwithforge.entities;

import javax.persistence.Entity;
import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Version;
import java.lang.Override;
import java.util.Date;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import com.github.thofis.blogwithforge.entities.Blogger;
import javax.persistence.ManyToOne;
import com.github.thofis.blogwithforge.entities.Comment;
import java.util.Set;
import java.util.HashSet;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;

@Entity
public class BlogEntry implements Serializable
{

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "id", updatable = false, nullable = false)
   private Long id = null;
   @Version
   @Column(name = "version")
   private int version = 0;

   @Column
   private String title;

   @Column
   private String content;

   @Temporal(TemporalType.DATE)
   private Date createdAt;

   @ManyToOne
   private Blogger blogger;

   @OneToMany(mappedBy = "blogEntry", cascade = CascadeType.ALL, orphanRemoval = true)
   private Set<Comment> comments = new HashSet<Comment>();

   public Long getId()
   {
      return this.id;
   }

   public void setId(final Long id)
   {
      this.id = id;
   }

   public int getVersion()
   {
      return this.version;
   }

   public void setVersion(final int version)
   {
      this.version = version;
   }

   @Override
   public boolean equals(Object that)
   {
      if (this == that)
      {
         return true;
      }
      if (that == null)
      {
         return false;
      }
      if (getClass() != that.getClass())
      {
         return false;
      }
      if (id != null)
      {
         return id.equals(((BlogEntry) that).id);
      }
      return super.equals(that);
   }

   @Override
   public int hashCode()
   {
      if (id != null)
      {
         return id.hashCode();
      }
      return super.hashCode();
   }

   public String getTitle()
   {
      return this.title;
   }

   public void setTitle(final String title)
   {
      this.title = title;
   }

   public String getContent()
   {
      return this.content;
   }

   public void setContent(final String content)
   {
      this.content = content;
   }

   public Date getCreatedAt()
   {
      return this.createdAt;
   }

   public void setCreatedAt(final Date createdAt)
   {
      this.createdAt = createdAt;
   }

   @Override
   public String toString()
   {
      String result = getClass().getSimpleName() + " ";
      if (title != null && !title.trim().isEmpty())
         result += "title: " + title;
      if (content != null && !content.trim().isEmpty())
         result += ", content: " + content;
      return result;
   }

   public Blogger getBlogger()
   {
      return this.blogger;
   }

   public void setBlogger(final Blogger blogger)
   {
      this.blogger = blogger;
   }

   public Set<Comment> getComments()
   {
      return this.comments;
   }

   public void setComments(final Set<Comment> comments)
   {
      this.comments = comments;
   }
}