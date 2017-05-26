package com.sgkhmjaes.jdias.web.rest;

import com.sgkhmjaes.jdias.JDiasApp;

import com.sgkhmjaes.jdias.domain.Photo;
import com.sgkhmjaes.jdias.repository.PhotoRepository;
import com.sgkhmjaes.jdias.repository.search.PhotoSearchRepository;
import com.sgkhmjaes.jdias.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the PhotoResource REST controller.
 *
 * @see PhotoResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JDiasApp.class)
public class PhotoResourceIntTest {

    private static final String DEFAULT_AUTHOR = "AAAAAAAAAA";
    private static final String UPDATED_AUTHOR = "BBBBBBBBBB";

    private static final Boolean DEFAULT_GUID = false;
    private static final Boolean UPDATED_GUID = true;

    private static final LocalDate DEFAULT_CREATEDAT = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CREATEDAT = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_REMOTEPHOTOPATH = "AAAAAAAAAA";
    private static final String UPDATED_REMOTEPHOTOPATH = "BBBBBBBBBB";

    private static final String DEFAULT_REMOTEPHOTONAME = "AAAAAAAAAA";
    private static final String UPDATED_REMOTEPHOTONAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_HEIGHT = 1;
    private static final Integer UPDATED_HEIGHT = 2;

    private static final Integer DEFAULT_WIDTH = 1;
    private static final Integer UPDATED_WIDTH = 2;

    private static final String DEFAULT_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_TEXT = "BBBBBBBBBB";

    private static final String DEFAULT_STATUSMESSAGEGUID = "AAAAAAAAAA";
    private static final String UPDATED_STATUSMESSAGEGUID = "BBBBBBBBBB";

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PhotoSearchRepository photoSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restPhotoMockMvc;

    private Photo photo;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PhotoResource photoResource = new PhotoResource(photoRepository, photoSearchRepository);
        this.restPhotoMockMvc = MockMvcBuilders.standaloneSetup(photoResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Photo createEntity(EntityManager em) {
        Photo photo = new Photo()
            .author(DEFAULT_AUTHOR)
            .guid(DEFAULT_GUID)
            .createdat(DEFAULT_CREATEDAT)
            .remotephotopath(DEFAULT_REMOTEPHOTOPATH)
            .remotephotoname(DEFAULT_REMOTEPHOTONAME)
            .height(DEFAULT_HEIGHT)
            .width(DEFAULT_WIDTH)
            .text(DEFAULT_TEXT)
            .statusmessageguid(DEFAULT_STATUSMESSAGEGUID);
        return photo;
    }

    @Before
    public void initTest() {
        photoSearchRepository.deleteAll();
        photo = createEntity(em);
    }

    @Test
    @Transactional
    public void createPhoto() throws Exception {
        int databaseSizeBeforeCreate = photoRepository.findAll().size();

        // Create the Photo
        restPhotoMockMvc.perform(post("/api/photos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(photo)))
            .andExpect(status().isCreated());

        // Validate the Photo in the database
        List<Photo> photoList = photoRepository.findAll();
        assertThat(photoList).hasSize(databaseSizeBeforeCreate + 1);
        Photo testPhoto = photoList.get(photoList.size() - 1);
        assertThat(testPhoto.getAuthor()).isEqualTo(DEFAULT_AUTHOR);
        assertThat(testPhoto.isGuid()).isEqualTo(DEFAULT_GUID);
        assertThat(testPhoto.getCreatedat()).isEqualTo(DEFAULT_CREATEDAT);
        assertThat(testPhoto.getRemotephotopath()).isEqualTo(DEFAULT_REMOTEPHOTOPATH);
        assertThat(testPhoto.getRemotephotoname()).isEqualTo(DEFAULT_REMOTEPHOTONAME);
        assertThat(testPhoto.getHeight()).isEqualTo(DEFAULT_HEIGHT);
        assertThat(testPhoto.getWidth()).isEqualTo(DEFAULT_WIDTH);
        assertThat(testPhoto.getText()).isEqualTo(DEFAULT_TEXT);
        assertThat(testPhoto.getStatusmessageguid()).isEqualTo(DEFAULT_STATUSMESSAGEGUID);

        // Validate the Photo in Elasticsearch
        Photo photoEs = photoSearchRepository.findOne(testPhoto.getId());
        assertThat(photoEs).isEqualToComparingFieldByField(testPhoto);
    }

    @Test
    @Transactional
    public void createPhotoWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = photoRepository.findAll().size();

        // Create the Photo with an existing ID
        photo.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPhotoMockMvc.perform(post("/api/photos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(photo)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Photo> photoList = photoRepository.findAll();
        assertThat(photoList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllPhotos() throws Exception {
        // Initialize the database
        photoRepository.saveAndFlush(photo);

        // Get all the photoList
        restPhotoMockMvc.perform(get("/api/photos?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(photo.getId().intValue())))
            .andExpect(jsonPath("$.[*].author").value(hasItem(DEFAULT_AUTHOR.toString())))
            .andExpect(jsonPath("$.[*].guid").value(hasItem(DEFAULT_GUID.booleanValue())))
            .andExpect(jsonPath("$.[*].createdat").value(hasItem(DEFAULT_CREATEDAT.toString())))
            .andExpect(jsonPath("$.[*].remotephotopath").value(hasItem(DEFAULT_REMOTEPHOTOPATH.toString())))
            .andExpect(jsonPath("$.[*].remotephotoname").value(hasItem(DEFAULT_REMOTEPHOTONAME.toString())))
            .andExpect(jsonPath("$.[*].height").value(hasItem(DEFAULT_HEIGHT)))
            .andExpect(jsonPath("$.[*].width").value(hasItem(DEFAULT_WIDTH)))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT.toString())))
            .andExpect(jsonPath("$.[*].statusmessageguid").value(hasItem(DEFAULT_STATUSMESSAGEGUID.toString())));
    }

    @Test
    @Transactional
    public void getPhoto() throws Exception {
        // Initialize the database
        photoRepository.saveAndFlush(photo);

        // Get the photo
        restPhotoMockMvc.perform(get("/api/photos/{id}", photo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(photo.getId().intValue()))
            .andExpect(jsonPath("$.author").value(DEFAULT_AUTHOR.toString()))
            .andExpect(jsonPath("$.guid").value(DEFAULT_GUID.booleanValue()))
            .andExpect(jsonPath("$.createdat").value(DEFAULT_CREATEDAT.toString()))
            .andExpect(jsonPath("$.remotephotopath").value(DEFAULT_REMOTEPHOTOPATH.toString()))
            .andExpect(jsonPath("$.remotephotoname").value(DEFAULT_REMOTEPHOTONAME.toString()))
            .andExpect(jsonPath("$.height").value(DEFAULT_HEIGHT))
            .andExpect(jsonPath("$.width").value(DEFAULT_WIDTH))
            .andExpect(jsonPath("$.text").value(DEFAULT_TEXT.toString()))
            .andExpect(jsonPath("$.statusmessageguid").value(DEFAULT_STATUSMESSAGEGUID.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingPhoto() throws Exception {
        // Get the photo
        restPhotoMockMvc.perform(get("/api/photos/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePhoto() throws Exception {
        // Initialize the database
        photoRepository.saveAndFlush(photo);
        photoSearchRepository.save(photo);
        int databaseSizeBeforeUpdate = photoRepository.findAll().size();

        // Update the photo
        Photo updatedPhoto = photoRepository.findOne(photo.getId());
        updatedPhoto
            .author(UPDATED_AUTHOR)
            .guid(UPDATED_GUID)
            .createdat(UPDATED_CREATEDAT)
            .remotephotopath(UPDATED_REMOTEPHOTOPATH)
            .remotephotoname(UPDATED_REMOTEPHOTONAME)
            .height(UPDATED_HEIGHT)
            .width(UPDATED_WIDTH)
            .text(UPDATED_TEXT)
            .statusmessageguid(UPDATED_STATUSMESSAGEGUID);

        restPhotoMockMvc.perform(put("/api/photos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedPhoto)))
            .andExpect(status().isOk());

        // Validate the Photo in the database
        List<Photo> photoList = photoRepository.findAll();
        assertThat(photoList).hasSize(databaseSizeBeforeUpdate);
        Photo testPhoto = photoList.get(photoList.size() - 1);
        assertThat(testPhoto.getAuthor()).isEqualTo(UPDATED_AUTHOR);
        assertThat(testPhoto.isGuid()).isEqualTo(UPDATED_GUID);
        assertThat(testPhoto.getCreatedat()).isEqualTo(UPDATED_CREATEDAT);
        assertThat(testPhoto.getRemotephotopath()).isEqualTo(UPDATED_REMOTEPHOTOPATH);
        assertThat(testPhoto.getRemotephotoname()).isEqualTo(UPDATED_REMOTEPHOTONAME);
        assertThat(testPhoto.getHeight()).isEqualTo(UPDATED_HEIGHT);
        assertThat(testPhoto.getWidth()).isEqualTo(UPDATED_WIDTH);
        assertThat(testPhoto.getText()).isEqualTo(UPDATED_TEXT);
        assertThat(testPhoto.getStatusmessageguid()).isEqualTo(UPDATED_STATUSMESSAGEGUID);

        // Validate the Photo in Elasticsearch
        Photo photoEs = photoSearchRepository.findOne(testPhoto.getId());
        assertThat(photoEs).isEqualToComparingFieldByField(testPhoto);
    }

    @Test
    @Transactional
    public void updateNonExistingPhoto() throws Exception {
        int databaseSizeBeforeUpdate = photoRepository.findAll().size();

        // Create the Photo

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restPhotoMockMvc.perform(put("/api/photos")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(photo)))
            .andExpect(status().isCreated());

        // Validate the Photo in the database
        List<Photo> photoList = photoRepository.findAll();
        assertThat(photoList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deletePhoto() throws Exception {
        // Initialize the database
        photoRepository.saveAndFlush(photo);
        photoSearchRepository.save(photo);
        int databaseSizeBeforeDelete = photoRepository.findAll().size();

        // Get the photo
        restPhotoMockMvc.perform(delete("/api/photos/{id}", photo.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean photoExistsInEs = photoSearchRepository.exists(photo.getId());
        assertThat(photoExistsInEs).isFalse();

        // Validate the database is empty
        List<Photo> photoList = photoRepository.findAll();
        assertThat(photoList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchPhoto() throws Exception {
        // Initialize the database
        photoRepository.saveAndFlush(photo);
        photoSearchRepository.save(photo);

        // Search the photo
        restPhotoMockMvc.perform(get("/api/_search/photos?query=id:" + photo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(photo.getId().intValue())))
            .andExpect(jsonPath("$.[*].author").value(hasItem(DEFAULT_AUTHOR.toString())))
            .andExpect(jsonPath("$.[*].guid").value(hasItem(DEFAULT_GUID.booleanValue())))
            .andExpect(jsonPath("$.[*].createdat").value(hasItem(DEFAULT_CREATEDAT.toString())))
            .andExpect(jsonPath("$.[*].remotephotopath").value(hasItem(DEFAULT_REMOTEPHOTOPATH.toString())))
            .andExpect(jsonPath("$.[*].remotephotoname").value(hasItem(DEFAULT_REMOTEPHOTONAME.toString())))
            .andExpect(jsonPath("$.[*].height").value(hasItem(DEFAULT_HEIGHT)))
            .andExpect(jsonPath("$.[*].width").value(hasItem(DEFAULT_WIDTH)))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT.toString())))
            .andExpect(jsonPath("$.[*].statusmessageguid").value(hasItem(DEFAULT_STATUSMESSAGEGUID.toString())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Photo.class);
        Photo photo1 = new Photo();
        photo1.setId(1L);
        Photo photo2 = new Photo();
        photo2.setId(photo1.getId());
        assertThat(photo1).isEqualTo(photo2);
        photo2.setId(2L);
        assertThat(photo1).isNotEqualTo(photo2);
        photo1.setId(null);
        assertThat(photo1).isNotEqualTo(photo2);
    }
}
