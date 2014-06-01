package com.github.thofis.blogwithforge.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.github.thofis.blogwithforge.entities.Blogger;
import com.github.thofis.blogwithforge.entities.BlogEntry;
import java.util.Iterator;

/**
 * Backing bean for Blogger entities.
 * <p>
 * This class provides CRUD functionality for all Blogger entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class BloggerBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving Blogger entities
    */

   private Long id;

   public Long getId()
   {
      return this.id;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   private Blogger blogger;

   public Blogger getBlogger()
   {
      return this.blogger;
   }

   @Inject
   private Conversation conversation;

   @PersistenceContext(type = PersistenceContextType.EXTENDED)
   private EntityManager entityManager;

   public String create()
   {

      this.conversation.begin();
      return "create?faces-redirect=true";
   }

   public void retrieve()
   {

      if (FacesContext.getCurrentInstance().isPostback())
      {
         return;
      }

      if (this.conversation.isTransient())
      {
         this.conversation.begin();
      }

      if (this.id == null)
      {
         this.blogger = this.example;
      }
      else
      {
         this.blogger = findById(getId());
      }
   }

   public Blogger findById(Long id)
   {

      return this.entityManager.find(Blogger.class, id);
   }

   /*
    * Support updating and deleting Blogger entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.blogger);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.blogger);
            return "view?faces-redirect=true&id=" + this.blogger.getId();
         }
      }
      catch (Exception e)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
         return null;
      }
   }

   public String delete()
   {
      this.conversation.end();

      try
      {
         Blogger deletableEntity = findById(getId());
         Iterator<BlogEntry> iterBlogEntries = deletableEntity.getBlogEntries().iterator();
         for (; iterBlogEntries.hasNext();)
         {
            BlogEntry nextInBlogEntries = iterBlogEntries.next();
            nextInBlogEntries.setBlogger(null);
            iterBlogEntries.remove();
            this.entityManager.merge(nextInBlogEntries);
         }
         this.entityManager.remove(deletableEntity);
         this.entityManager.flush();
         return "search?faces-redirect=true";
      }
      catch (Exception e)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
         return null;
      }
   }

   /*
    * Support searching Blogger entities with pagination
    */

   private int page;
   private long count;
   private List<Blogger> pageItems;

   private Blogger example = new Blogger();

   public int getPage()
   {
      return this.page;
   }

   public void setPage(int page)
   {
      this.page = page;
   }

   public int getPageSize()
   {
      return 10;
   }

   public Blogger getExample()
   {
      return this.example;
   }

   public void setExample(Blogger example)
   {
      this.example = example;
   }

   public void search()
   {
      this.page = 0;
   }

   public void paginate()
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

      // Populate this.count

      CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
      Root<Blogger> root = countCriteria.from(Blogger.class);
      countCriteria = countCriteria.select(builder.count(root)).where(
            getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria)
            .getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<Blogger> criteria = builder.createQuery(Blogger.class);
      root = criteria.from(Blogger.class);
      TypedQuery<Blogger> query = this.entityManager.createQuery(criteria
            .select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(
            getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<Blogger> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      String firstName = this.example.getFirstName();
      if (firstName != null && !"".equals(firstName))
      {
         predicatesList.add(builder.like(root.<String> get("firstName"), '%' + firstName + '%'));
      }
      String lastName = this.example.getLastName();
      if (lastName != null && !"".equals(lastName))
      {
         predicatesList.add(builder.like(root.<String> get("lastName"), '%' + lastName + '%'));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<Blogger> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back Blogger entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<Blogger> getAll()
   {

      CriteriaQuery<Blogger> criteria = this.entityManager
            .getCriteriaBuilder().createQuery(Blogger.class);
      return this.entityManager.createQuery(
            criteria.select(criteria.from(Blogger.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final BloggerBean ejbProxy = this.sessionContext.getBusinessObject(BloggerBean.class);

      return new Converter()
      {

         @Override
         public Object getAsObject(FacesContext context,
               UIComponent component, String value)
         {

            return ejbProxy.findById(Long.valueOf(value));
         }

         @Override
         public String getAsString(FacesContext context,
               UIComponent component, Object value)
         {

            if (value == null)
            {
               return "";
            }

            return String.valueOf(((Blogger) value).getId());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private Blogger add = new Blogger();

   public Blogger getAdd()
   {
      return this.add;
   }

   public Blogger getAdded()
   {
      Blogger added = this.add;
      this.add = new Blogger();
      return added;
   }
}