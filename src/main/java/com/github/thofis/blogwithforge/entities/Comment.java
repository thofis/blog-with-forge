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
import com.github.thofis.blogwithforge.entities.BlogEntry;
import javax.persistence.ManyToOne;

@Entity
public class Comment implements Serializable
{

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "id", updatable = false, nullable = false)
   private Long id = null;
   @Version
   @Column(name = "version")
   private int version = 0;

   @Column
   private String originator;

   @Temporal(TemporalType.DATE)
   private Date createdAt;

   @Column
   private String content;

   @ManyToOne
   private BlogEntry blogEntry;

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
         return id.equals(((Comment) that).id);
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

   public String getOriginator()
   {
      return this.originator;
   }

   public void setOriginator(final String originator)
   {
      this.originator = originator;
   }

   public Date getCreatedAt()
   {
      return this.createdAt;
   }

   public void setCreatedAt(final Date createdAt)
   {
      this.createdAt = createdAt;
   }

   public String getContent()
   {
      return this.content;
   }

   public void setContent(final String content)
   {
      this.content = content;
   }

   @Override
   public String toString()
   {
      String result = getClass().getSimpleName() + " ";
      if (originator != null && !originator.trim().isEmpty())
         result += "originator: " + originator;
      if (content != null && !content.trim().isEmpty())
         result += ", content: " + content;
      return result;
   }

   public BlogEntry getBlogEntry()
   {
      return this.blogEntry;
   }

   public void setBlogEntry(final BlogEntry blogEntry)
   {
      this.blogEntry = blogEntry;
   }
}