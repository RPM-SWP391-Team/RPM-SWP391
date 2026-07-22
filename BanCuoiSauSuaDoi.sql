USE [master]
GO
/****** Object:  Database [RemotePatientMonitoringVer2]    Script Date: 7/21/2026 6:25:42 PM ******/
CREATE DATABASE [RemotePatientMonitoringVer2]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'RemotePatientMonitoringVer2', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL15.MSSQLSERVER\MSSQL\DATA\RemotePatientMonitoringVer2.mdf' , SIZE = 8192KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'RemotePatientMonitoringVer2_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL15.MSSQLSERVER\MSSQL\DATA\RemotePatientMonitoringVer2_log.ldf' , SIZE = 8192KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET COMPATIBILITY_LEVEL = 150
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [RemotePatientMonitoringVer2].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ARITHABORT OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET AUTO_CLOSE ON 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET  ENABLE_BROKER 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET RECOVERY SIMPLE 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET  MULTI_USER 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET DB_CHAINING OFF 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET QUERY_STORE = OFF
GO
USE [RemotePatientMonitoringVer2]
GO
/****** Object:  Table [dbo].[accounts]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[accounts](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[email] [varchar](255) NOT NULL,
	[password_hash] [varchar](255) NOT NULL,
	[role] [varchar](20) NOT NULL,
	[is_email_verified] [bit] NOT NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
	[registration_details] [varchar](4000) NULL,
 CONSTRAINT [PK_accounts] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[alert_thresholds]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[alert_thresholds](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[hospital_id] [int] NOT NULL,
	[patient_id] [int] NULL,
	[scope] [varchar](10) NOT NULL,
	[metric_type] [varchar](50) NOT NULL,
	[glucose_hypo_threshold] [decimal](4, 2) NULL,
	[glucose_normal_max] [decimal](4, 2) NULL,
	[glucose_high_max] [decimal](4, 2) NULL,
	[systolic_normal_max] [int] NULL,
	[systolic_warning_min] [int] NULL,
	[systolic_warning_max] [int] NULL,
	[systolic_danger_min] [int] NULL,
	[systolic_danger_max] [int] NULL,
	[systolic_emergency_threshold] [int] NULL,
	[diastolic_normal_max] [int] NULL,
	[diastolic_warning_min] [int] NULL,
	[diastolic_warning_max] [int] NULL,
	[diastolic_danger_min] [int] NULL,
	[diastolic_danger_max] [int] NULL,
	[diastolic_emergency_threshold] [int] NULL,
	[created_by_doctor_id] [int] NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_alert_thresholds] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[alerts]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[alerts](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NOT NULL,
	[health_log_id] [int] NULL,
	[alert_level] [int] NOT NULL,
	[alert_color] [varchar](10) NOT NULL,
	[metric_type] [varchar](30) NOT NULL,
	[metric_value] [nvarchar](200) NOT NULL,
	[threshold_violated] [nvarchar](200) NOT NULL,
	[alert_message] [nvarchar](500) NOT NULL,
	[is_resolved] [bit] NOT NULL,
	[resolved_at] [datetime2](7) NULL,
	[resolved_by_doctor_id] [int] NULL,
	[resolution_notes] [nvarchar](500) NULL,
	[triggered_at] [datetime2](7) NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_alerts] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[appointments]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[appointments](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NOT NULL,
	[appointment_time] [datetime2](7) NOT NULL,
	[location] [nvarchar](255) NULL,
	[status] [varchar](15) NOT NULL,
	[appointment_type] [varchar](20) NOT NULL,
	[created_by] [varchar](10) NOT NULL,
	[patient_requested_time] [datetime2](7) NULL,
	[patient_request_reason] [nvarchar](max) NULL,
	[doctor_note] [nvarchar](max) NULL,
	[rejection_reason] [nvarchar](500) NULL,
	[reminder_sent_2days] [bit] NOT NULL,
	[completed_at] [datetime2](7) NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_appointments] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[audit_trails]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[audit_trails](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[actor_type] [varchar](20) NOT NULL,
	[actor_id] [int] NOT NULL,
	[action] [varchar](100) NOT NULL,
	[target_table] [varchar](100) NOT NULL,
	[target_record_id] [int] NOT NULL,
	[old_value] [nvarchar](max) NULL,
	[new_value] [nvarchar](max) NULL,
	[ip_address] [varchar](100) NULL,
	[device_info] [varchar](500) NULL,
	[notes] [nvarchar](500) NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_audit_trails] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[clinical_records]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[clinical_records](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NOT NULL,
	[examination_date] [datetime2](7) NOT NULL,
	[weight_kg] [decimal](5, 2) NULL,
	[height_cm] [decimal](5, 2) NULL,
	[bmi] [decimal](5, 2) NULL,
	[systolic_bp] [int] NULL,
	[diastolic_bp] [int] NULL,
	[heart_rate] [int] NULL,
	[fasting_glucose] [decimal](4, 2) NULL,
	[hba1c] [decimal](5, 2) NULL,
	[diagnosis] [nvarchar](max) NULL,
	[symptoms_noted] [nvarchar](max) NULL,
	[doctor_notes] [nvarchar](max) NULL,
	[is_initial_exam] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_clinical_records] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[change_requests]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[change_requests](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NOT NULL,
	[request_type] [varchar](20) NOT NULL,
	[patient_reason] [nvarchar](max) NULL,
	[status] [varchar](15) NOT NULL,
	[doctor_response] [nvarchar](max) NULL,
	[processed_at] [datetime2](7) NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_change_requests] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[daily_health_logs]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[daily_health_logs](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[log_date] [date] NOT NULL,
	[log_time] [datetime2](7) NOT NULL,
	[log_type] [varchar](10) NOT NULL,
	[systolic_bp] [int] NULL,
	[diastolic_bp] [int] NULL,
	[heart_rate] [int] NULL,
	[glucose_level] [decimal](4, 2) NULL,
	[input_method] [varchar](10) NOT NULL,
	[image_url] [varchar](500) NULL,
	[is_ocr_validated] [bit] NOT NULL,
	[alert_level] [varchar](10) NULL,
	[patient_notes] [nvarchar](500) NULL,
	[is_alert_processed] [bit] NOT NULL,
	[alert_processed_at] [datetime2](7) NULL,
	[alert_processed_by] [int] NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_daily_health_logs] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[diet_logs]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[diet_logs](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[food_id] [int] NOT NULL,
	[log_date] [date] NOT NULL,
	[meal_type] [varchar](10) NOT NULL,
	[quantity_g] [decimal](6, 2) NOT NULL,
	[logged_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_diet_logs] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[disease_profiles]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[disease_profiles](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[profile_code] [varchar](20) NOT NULL,
	[profile_name] [nvarchar](100) NOT NULL,
	[description] [nvarchar](500) NULL,
	[requires_bp_input] [bit] NOT NULL,
	[requires_glucose_input] [bit] NOT NULL,
	[is_active] [bit] NOT NULL,
 CONSTRAINT [PK_disease_profiles] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[doctors]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[doctors](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[account_id] [int] NOT NULL,
	[hospital_id] [int] NOT NULL,
	[doctor_code] [varchar](50) NOT NULL,
	[full_name] [nvarchar](255) NOT NULL,
	[gender] [varchar](10) NULL,
	[phone] [varchar](20) NOT NULL,
	[specialty] [nvarchar](255) NULL,
	[capacity_limit] [int] NOT NULL,
	[current_patient_count] [int] NOT NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_doctors] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[emergency_guides]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[emergency_guides](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[hospital_id] [int] NOT NULL,
	[alert_level] [varchar](10) NOT NULL,
	[metric_type] [varchar](30) NOT NULL,
	[title] [nvarchar](255) NOT NULL,
	[instruction_content] [nvarchar](max) NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_emergency_guides] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[emergency_protocols]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[emergency_protocols](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[hospital_id] [int] NOT NULL,
	[condition_type] [varchar](30) NOT NULL,
	[title] [nvarchar](255) NOT NULL,
	[warning_signs] [nvarchar](max) NULL,
	[instruction_content] [nvarchar](max) NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_emergency_protocols] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[exercise_guidelines]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[exercise_guidelines](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[avoid_content] [nvarchar](max) NULL,
	[created_at] [datetime2](6) NOT NULL,
	[is_active] [bit] NOT NULL,
	[recommended_content] [nvarchar](max) NULL,
	[title] [varchar](255) NOT NULL,
	[disease_profile_id] [int] NOT NULL,
	[hospital_id] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[exercise_logs]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[exercise_logs](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[log_date] [date] NOT NULL,
	[exercise_type] [nvarchar](100) NOT NULL,
	[duration_minutes] [int] NULL,
	[steps_count] [int] NULL,
	[logged_at] [datetime2](7) NOT NULL,
	[calories_burned] [float] NULL,
 CONSTRAINT [PK_exercise_logs] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[foods_dictionary]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[foods_dictionary](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[food_code] [varchar](50) NOT NULL,
	[food_name] [nvarchar](255) NOT NULL,
	[english_name] [nvarchar](255) NULL,
	[water_g] [decimal](5, 2) NULL,
	[energy_kcal] [int] NULL,
	[protein_g] [decimal](5, 2) NULL,
	[lipid_g] [decimal](5, 2) NULL,
	[glucid_g] [decimal](5, 2) NULL,
	[celluloza_g] [decimal](5, 2) NULL,
	[ash_g] [decimal](5, 2) NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[hospitals]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[hospitals](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[account_id] [int] NOT NULL,
	[hospital_code] [varchar](50) NOT NULL,
	[full_name] [nvarchar](255) NOT NULL,
	[address] [nvarchar](500) NULL,
	[phone] [varchar](20) NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_hospitals] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[medication_logs]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[medication_logs](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_medication_id] [int] NOT NULL,
	[log_date] [date] NOT NULL,
	[is_taken] [bit] NOT NULL,
	[taken_at] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[notifications]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[notifications](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[recipient_type] [varchar](255) NULL,
	[recipient_id] [int] NOT NULL,
	[notification_type] [varchar](255) NULL,
	[title] [nvarchar](255) NOT NULL,
	[content] [nvarchar](1000) NULL,
	[channel] [varchar](255) NULL,
	[related_entity_type] [varchar](50) NULL,
	[related_entity_id] [int] NULL,
	[status] [varchar](255) NULL,
	[is_read] [bit] NOT NULL,
	[sent_at] [datetime2](7) NULL,
	[read_at] [datetime2](7) NULL,
	[created_at] [datetime2](7) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NULL,
 CONSTRAINT [PK_notifications] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[nutrition_rules]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[nutrition_rules](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NOT NULL,
	[max_calories_per_day] [int] NOT NULL,
	[max_carbs_g] [decimal](7, 2) NOT NULL,
	[max_salt_g] [decimal](6, 2) NOT NULL,
	[min_fiber_g] [decimal](6, 2) NOT NULL,
	[max_fat_g] [decimal](7, 2) NULL,
	[min_protein_g] [decimal](7, 2) NULL,
	[daily_water_ml] [int] NULL,
	[additional_notes] [nvarchar](max) NULL,
	[is_current] [bit] NOT NULL,
	[effective_from] [date] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_nutrition_rules] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[otp_codes]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[otp_codes](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[email] [varchar](255) NOT NULL,
	[otp_code] [varchar](255) NULL,
	[otp_type] [varchar](255) NULL,
	[expires_at] [datetime2](7) NOT NULL,
	[is_used] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_otp_codes] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[patient_medications]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[patient_medications](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[medicine_name] [nvarchar](255) NOT NULL,
	[dosage] [nvarchar](100) NOT NULL,
	[scheduled_time] [varchar](10) NOT NULL,
	[is_active] [bit] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
	[treatment_plan_id] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[patients]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[patients](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[account_id] [int] NOT NULL,
	[hospital_id] [int] NOT NULL,
	[doctor_id] [int] NULL,
	[disease_profile_id] [int] NULL,
	[patient_code] [varchar](50) NULL,
	[full_name] [nvarchar](255) NOT NULL,
	[date_of_birth] [date] NULL,
	[gender] [varchar](10) NULL,
	[phone] [varchar](20) NOT NULL,
	[address] [nvarchar](500) NULL,
	[emergency_contact_name] [nvarchar](255) NULL,
	[emergency_contact_phone] [varchar](20) NULL,
	[status] [varchar](20) NOT NULL,
	[registration_source] [varchar](20) NOT NULL,
	[is_active] [bit] NOT NULL,
	[onboarded_at] [datetime2](7) NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_patients] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[system_logs]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[system_logs](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[log_level] [varchar](10) NOT NULL,
	[module_name] [varchar](100) NOT NULL,
	[event_code] [varchar](100) NULL,
	[message] [nvarchar](max) NOT NULL,
	[related_patient_id] [int] NULL,
	[related_doctor_id] [int] NULL,
	[related_entity_type] [varchar](50) NULL,
	[related_entity_id] [int] NULL,
	[additional_data] [nvarchar](max) NULL,
	[created_at] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_system_logs] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[treatment_plans]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[treatment_plans](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[doctor_id] [int] NOT NULL,
	[nutrition_rule_id] [int] NULL,
	[target_systolic_bp] [int] NULL,
	[target_diastolic_bp] [int] NULL,
	[target_fasting_glucose] [numeric](6, 2) NULL,
	[target_hba1c] [decimal](5, 2) NULL,
	[target_weight_kg] [decimal](5, 2) NULL,
	[target_steps] [int] NULL,
	[target_sleep_hours] [decimal](4, 2) NULL,
	[medical_order] [nvarchar](max) NULL,
	[exercise_goal] [nvarchar](500) NULL,
	[additional_notes] [nvarchar](max) NULL,
	[is_current] [bit] NOT NULL,
	[effective_from] [date] NOT NULL,
	[created_at] [datetime2](7) NOT NULL,
	[updated_at] [datetime2](7) NOT NULL,
	[baseline_diastolic_bp] [int] NULL,
	[baseline_fasting_glucose] [numeric](6, 2) NULL,
	[baseline_hba1c] [numeric](5, 2) NULL,
	[baseline_systolic_bp] [int] NULL,
	[baseline_weight_kg] [numeric](5, 2) NULL,
 CONSTRAINT [PK_treatment_plans] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[water_logs]    Script Date: 7/21/2026 6:25:42 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[water_logs](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[patient_id] [int] NOT NULL,
	[log_date] [date] NOT NULL,
	[amount_ml] [int] NOT NULL,
	[logged_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET IDENTITY_INSERT [dbo].[accounts] ON 
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (1, N'admin@bvdktrunguong-mau.vn', N'hashed_pwd_admin', N'HOSPITAL_ADMIN', 1, 1, CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (2, N'bs.an@hospital.vn', N'hashed_pwd_123', N'DOCTOR', 1, 1, CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (3, N'bs.binh@hospital.vn', N'hashed_pwd_456', N'DOCTOR', 1, 1, CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (4, N'benh.nhan.1@gmail.com', N'hashed_pwd_789', N'PATIENT', 1, 1, CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (5, N'benh.nhan.2@gmail.com', N'hashed_pwd_000', N'PATIENT', 1, 1, CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (6, N'benh.nhan.3@gmail.com', N'hashed_pwd_111', N'PATIENT', 1, 1, CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (7, N'benh.nhan.4@gmail.com', N'hashed_pwd_222', N'PATIENT', 1, 1, CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), CAST(N'2026-06-27T11:31:39.0878836' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (8, N'domanht7@gmail.com', N'$2a$10$q2Dg2TCnRDqQqGmLVEUviuiWL8/vlbm.wm1vhRK52mtzoy/IK/NxG', N'PATIENT', 1, 1, CAST(N'2026-06-27T16:22:16.5290539' AS DateTime2), CAST(N'2026-06-27T20:21:03.9363881' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (9, N'admin@gmail.com', N'$2a$10$6HbYYI7xDMq7h5vnr.5tJeEJbXVWDZ0I5entlQE..SSvyZygMk6qK', N'HOSPITAL_ADMIN', 1, 1, CAST(N'2026-06-27T17:55:25.4212231' AS DateTime2), CAST(N'2026-06-27T17:55:25.4212231' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (10, N'doctor1@rpm.com', N'$2a$10$72nQ6T5.R2zYlT.5p0X97eG7.6X55M5.0.3n6rY4H31F5J5.5O', N'DOCTOR', 0, 1, CAST(N'2026-06-27T18:43:26.6533333' AS DateTime2), CAST(N'2026-06-27T18:43:26.6553046' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (11, N'doctor2@rpm.com', N'$2a$10$72nQ6T5.R2zYlT.5p0X97eG7.6X55M5.0.3n6rY4H31F5J5.5O', N'DOCTOR', 0, 1, CAST(N'2026-06-27T18:43:26.6600000' AS DateTime2), CAST(N'2026-06-27T18:43:26.6618516' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (12, N'domanht8@gmail.com', N'$2a$10$yfUXJy8XGwlSrJ6PsPvKoOW4HA.iPiDV/ttOBq8qQOgDV9IHF4ZJi', N'DOCTOR', 1, 1, CAST(N'2026-06-27T18:50:28.2717073' AS DateTime2), CAST(N'2026-06-27T18:50:28.2717073' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (13, N'domanht10@gmail.com', N'$2a$10$1ORLJwzvgL7rPyexmOJASenLs5ve2XZdcl0VS1ffbnm73GU6FisUm', N'PATIENT', 1, 1, CAST(N'2026-07-03T11:30:14.9940686' AS DateTime2), CAST(N'2026-07-03T11:30:14.9940686' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (14, N'domanht13@gmail.com', N'$2a$10$7vCcQRCSwLKHrgY2VmGlXuoNXCIxqXbFTOe7z.3ZC2H6ugqm45rOq', N'PATIENT', 1, 1, CAST(N'2026-07-13T22:42:06.3673870' AS DateTime2), CAST(N'2026-07-13T22:42:06.3673870' AS DateTime2), NULL)
GO
INSERT [dbo].[accounts] ([id], [email], [password_hash], [role], [is_email_verified], [is_active], [created_at], [updated_at], [registration_details]) VALUES (15, N'domanht9@gmail.com', N'$2a$10$u0h7ZgbGu3dUpT0Mg.I.n.jqbp30H0ExPEoPjJS6/lBjwcgnhrBI.', N'PATIENT', 0, 1, CAST(N'2026-07-13T22:46:00.8337166' AS DateTime2), CAST(N'2026-07-13T22:46:00.8337166' AS DateTime2), N'{"emergencyContactPhone":"0389765222","address":"Ha Noi","gender":"FEMALE","phone":"0389765121","emergencyContactName":"Đ? M?nh To","fullName":"Lê Trung Th?ng","dateOfBirth":"2005-10-26"}')
GO
SET IDENTITY_INSERT [dbo].[accounts] OFF
GO
SET IDENTITY_INSERT [dbo].[alert_thresholds] ON 
GO
INSERT [dbo].[alert_thresholds] ([id], [hospital_id], [patient_id], [scope], [metric_type], [glucose_hypo_threshold], [glucose_normal_max], [glucose_high_max], [systolic_normal_max], [systolic_warning_min], [systolic_warning_max], [systolic_danger_min], [systolic_danger_max], [systolic_emergency_threshold], [diastolic_normal_max], [diastolic_warning_min], [diastolic_warning_max], [diastolic_danger_min], [diastolic_danger_max], [diastolic_emergency_threshold], [created_by_doctor_id], [created_at], [updated_at]) VALUES (1, 1, NULL, N'HOSPITAL', N'COMBINED', CAST(4.40 AS Decimal(4, 2)), CAST(10.00 AS Decimal(4, 2)), CAST(16.00 AS Decimal(4, 2)), 120, 130, 139, 140, 179, 180, 80, 85, 89, 90, 109, 110, NULL, CAST(N'2026-06-27T11:31:39.1038953' AS DateTime2), CAST(N'2026-06-27T11:31:39.1038953' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[alert_thresholds] OFF
GO
SET IDENTITY_INSERT [dbo].[appointments] ON 
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (1, 1, 7, CAST(N'2026-06-28T19:17:00.0000000' AS DateTime2), NULL, N'PENDING', N'INITIAL', N'PATIENT', NULL, N'a', NULL, NULL, 0, NULL, CAST(N'2026-06-27T19:17:32.9289512' AS DateTime2), CAST(N'2026-06-27T19:17:32.9289512' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (3, 2, 7, CAST(N'2026-07-06T11:32:00.0000000' AS DateTime2), NULL, N'PENDING', N'INITIAL', N'PATIENT', NULL, N'đẹp trai', NULL, NULL, 0, NULL, CAST(N'2026-07-03T11:33:05.4372954' AS DateTime2), CAST(N'2026-07-03T11:33:05.4372954' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (16, 1, 5, CAST(N'2026-07-10T10:00:00.0000000' AS DateTime2), N'Phòng khám 102 - Nhà A', N'COMPLETED', N'CHECKUP', N'PATIENT', NULL, N'Khám định kỳ theo dõi huyết áp', N'Huyết áp ổn định tốt. Tiếp tục dùng thuốc Amlodipine 5mg đúng giờ, giảm ăn mặn và tập thể dục 30 phút mỗi ngày.', NULL, 0, CAST(N'2026-07-10T11:00:00.0000000' AS DateTime2), CAST(N'2026-07-12T23:47:38.4966667' AS DateTime2), CAST(N'2026-07-12T23:47:38.4966667' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (17, 1, 6, CAST(N'2026-07-05T14:00:00.0000000' AS DateTime2), N'Phòng tư vấn trực tuyến', N'COMPLETED', N'CHECKUP', N'PATIENT', NULL, N'Đường huyết dao động thất thường buổi sáng', N'Đường huyết đói hơi cao (7.2 mmol/L). Điều chỉnh thực đơn sáng: bớt ăn tinh bột nhanh, tăng cường rau xanh. Uống bổ sung Metformin theo đúng liều lượng.', NULL, 0, CAST(N'2026-07-05T14:45:00.0000000' AS DateTime2), CAST(N'2026-07-12T23:47:38.4966667' AS DateTime2), CAST(N'2026-07-12T23:47:38.4966667' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (18, 1, 5, CAST(N'2026-07-15T09:00:00.0000000' AS DateTime2), NULL, N'REJECTED', N'CHECKUP', N'PATIENT', NULL, N'Đăng ký khám lại', NULL, N'Bác sĩ bận lịch phẫu thuật đột xuất, vui lòng chọn khung giờ khác hoặc đăng ký bác sĩ khác.', 0, NULL, CAST(N'2026-07-12T23:47:38.5000000' AS DateTime2), CAST(N'2026-07-12T23:47:38.5000000' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (19, 2, 5, CAST(N'2026-07-10T10:00:00.0000000' AS DateTime2), N'Phòng khám 102 - Nhà A', N'COMPLETED', N'CHECKUP', N'PATIENT', NULL, N'Khám định kỳ theo dõi huyết áp', N'Huyết áp ổn định tốt. Tiếp tục dùng thuốc Amlodipine 5mg đúng giờ, giảm ăn mặn và tập thể dục 30 phút mỗi ngày.', NULL, 0, CAST(N'2026-07-10T11:00:00.0000000' AS DateTime2), CAST(N'2026-07-12T23:47:38.5000000' AS DateTime2), CAST(N'2026-07-12T23:47:38.5000000' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (20, 2, 6, CAST(N'2026-07-05T14:00:00.0000000' AS DateTime2), N'Phòng tư vấn trực tuyến', N'COMPLETED', N'CHECKUP', N'PATIENT', NULL, N'Đường huyết dao động thất thường buổi sáng', N'Đường huyết đói hơi cao (7.2 mmol/L). Điều chỉnh thực đơn sáng: bớt ăn tinh bột nhanh, tăng cường rau xanh. Uống bổ sung Metformin theo đúng liều lượng.', NULL, 0, CAST(N'2026-07-05T14:45:00.0000000' AS DateTime2), CAST(N'2026-07-12T23:47:38.5000000' AS DateTime2), CAST(N'2026-07-12T23:47:38.5000000' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (21, 2, 5, CAST(N'2026-07-15T09:00:00.0000000' AS DateTime2), NULL, N'REJECTED', N'CHECKUP', N'PATIENT', NULL, N'Đăng ký khám lại', NULL, N'Bác sĩ bận lịch phẫu thuật đột xuất, vui lòng chọn khung giờ khác hoặc đăng ký bác sĩ khác.', 0, NULL, CAST(N'2026-07-12T23:47:38.5000000' AS DateTime2), CAST(N'2026-07-12T23:47:38.5000000' AS DateTime2))
GO
INSERT [dbo].[appointments] ([id], [patient_id], [doctor_id], [appointment_time], [location], [status], [appointment_type], [created_by], [patient_requested_time], [patient_request_reason], [doctor_note], [rejection_reason], [reminder_sent_2days], [completed_at], [created_at], [updated_at]) VALUES (22, 3, 7, CAST(N'2026-07-14T13:43:00.0000000' AS DateTime2), NULL, N'PENDING', N'INITIAL', N'PATIENT', NULL, N'đái tháo đường', NULL, NULL, 0, NULL, CAST(N'2026-07-13T22:44:04.9221354' AS DateTime2), CAST(N'2026-07-13T22:44:04.9221354' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[appointments] OFF
GO
SET IDENTITY_INSERT [dbo].[audit_trails] ON 
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (1, N'DOCTOR', 7, N'ASSIGN_PATIENT_TO_DOCTOR', N'patients', 1, NULL, N'{"doctor_id":7, "status":"TREATING"}', NULL, NULL, NULL, CAST(N'2026-06-27T19:29:00.4809353' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (2, N'DOCTOR', 7, N'ASSIGN_PATIENT_TO_DOCTOR', N'patients', 2, NULL, N'{"doctor_id":7, "status":"TREATING"}', NULL, NULL, NULL, CAST(N'2026-07-13T22:38:53.2605384' AS DateTime2))
GO
INSERT [dbo].[audit_trails] ([id], [actor_type], [actor_id], [action], [target_table], [target_record_id], [old_value], [new_value], [ip_address], [device_info], [notes], [created_at]) VALUES (3, N'DOCTOR', 7, N'ASSIGN_PATIENT_TO_DOCTOR', N'patients', 3, NULL, N'{"doctor_id":7, "status":"TREATING"}', NULL, NULL, NULL, CAST(N'2026-07-13T22:44:31.9226994' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[audit_trails] OFF
GO
SET IDENTITY_INSERT [dbo].[change_requests] ON 
GO
INSERT [dbo].[change_requests] ([id], [patient_id], [doctor_id], [request_type], [patient_reason], [status], [doctor_response], [processed_at], [created_at], [updated_at]) VALUES (1, 1, 7, N'DIET', N'ăn nhưng vẫn thấy đói - đã cập nhật', N'PENDING', NULL, NULL, CAST(N'2026-06-28T17:56:30.8989203' AS DateTime2), CAST(N'2026-07-20T08:17:59.9873049' AS DateTime2))
GO
INSERT [dbo].[change_requests] ([id], [patient_id], [doctor_id], [request_type], [patient_reason], [status], [doctor_response], [processed_at], [created_at], [updated_at]) VALUES (2, 1, 7, N'TREATMENT_PLAN', N'kgkuku', N'PENDING', NULL, NULL, CAST(N'2026-07-13T09:17:39.1554984' AS DateTime2), CAST(N'2026-07-13T09:17:39.1554984' AS DateTime2))
GO
INSERT [dbo].[change_requests] ([id], [patient_id], [doctor_id], [request_type], [patient_reason], [status], [doctor_response], [processed_at], [created_at], [updated_at]) VALUES (3, 1, 7, N'DIET', N'Thử nghiệm tạo mới yêu cầu thay đổi phác đồ', N'PENDING', NULL, NULL, CAST(N'2026-07-20T08:19:14.3424579' AS DateTime2), CAST(N'2026-07-20T08:19:14.3424579' AS DateTime2))
GO
INSERT [dbo].[change_requests] ([id], [patient_id], [doctor_id], [request_type], [patient_reason], [status], [doctor_response], [processed_at], [created_at], [updated_at]) VALUES (4, 1, 7, N'DIET', N'met', N'PENDING', NULL, NULL, CAST(N'2026-07-20T08:22:05.6931858' AS DateTime2), CAST(N'2026-07-20T08:22:05.6931858' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[change_requests] OFF
GO
SET IDENTITY_INSERT [dbo].[daily_health_logs] ON 
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (1, 1, CAST(N'2026-06-21' AS Date), CAST(N'2026-06-21T08:00:00.0000000' AS DateTime2), N'MORNING', 127, 82, 75, CAST(6.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-28T17:08:04.0766667' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (2, 1, CAST(N'2026-06-22' AS Date), CAST(N'2026-06-22T08:00:00.0000000' AS DateTime2), N'MORNING', 126, 81, 75, CAST(5.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-28T17:08:04.0766667' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (3, 1, CAST(N'2026-06-23' AS Date), CAST(N'2026-06-23T08:00:00.0000000' AS DateTime2), N'MORNING', 125, 80, 75, CAST(7.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-28T17:08:04.0766667' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (4, 1, CAST(N'2026-06-24' AS Date), CAST(N'2026-06-24T08:00:00.0000000' AS DateTime2), N'MORNING', 124, 84, 75, CAST(6.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-28T17:08:04.0766667' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (5, 1, CAST(N'2026-06-25' AS Date), CAST(N'2026-06-25T08:00:00.0000000' AS DateTime2), N'MORNING', 123, 83, 75, CAST(5.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-28T17:08:04.0766667' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (6, 1, CAST(N'2026-06-26' AS Date), CAST(N'2026-06-26T08:00:00.0000000' AS DateTime2), N'MORNING', 122, 82, 75, CAST(7.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-28T17:08:04.0766667' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (7, 1, CAST(N'2026-06-27' AS Date), CAST(N'2026-06-27T08:00:00.0000000' AS DateTime2), N'MORNING', 121, 81, 75, CAST(6.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, NULL, 1, NULL, NULL, CAST(N'2026-06-28T17:08:04.0766667' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (8, 1, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T17:40:53.6818685' AS DateTime2), N'MORNING', 300, 200, 250, NULL, N'MANUAL', NULL, 0, NULL, N'mệt', 0, NULL, NULL, CAST(N'2026-06-28T17:40:53.6818685' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (13, 1, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T17:45:46.8392552' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(4.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'mệt', 0, NULL, NULL, CAST(N'2026-06-28T17:45:46.8392552' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (15, 1, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T17:58:31.6493371' AS DateTime2), N'EVENING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'ko ao', 0, NULL, NULL, CAST(N'2026-06-28T17:58:31.6493371' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (16, 1, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T18:24:53.9716118' AS DateTime2), N'RANDOM', NULL, NULL, NULL, NULL, N'MANUAL', NULL, 0, NULL, N'', 0, NULL, NULL, CAST(N'2026-06-28T18:24:53.9716118' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (17, 1, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T18:25:15.1634600' AS DateTime2), N'RANDOM', NULL, NULL, NULL, NULL, N'MANUAL', NULL, 0, NULL, N'', 0, NULL, NULL, CAST(N'2026-06-28T18:25:15.1634600' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (18, 1, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T18:27:39.3945076' AS DateTime2), N'RANDOM', 80, 50, 23, NULL, N'MANUAL', NULL, 0, NULL, N'23', 0, NULL, NULL, CAST(N'2026-06-28T18:27:39.3945076' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (19, 1, CAST(N'2026-06-29' AS Date), CAST(N'2026-06-29T08:07:23.4644101' AS DateTime2), N'MORNING', 181, 100, 90, NULL, N'MANUAL', NULL, 0, NULL, N'meetj', 0, NULL, NULL, CAST(N'2026-06-29T08:07:23.4644101' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (20, 1, CAST(N'2026-07-03' AS Date), CAST(N'2026-07-03T09:17:00.8330586' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'khỏe', 0, NULL, NULL, CAST(N'2026-07-03T09:17:00.8330586' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (21, 1, CAST(N'2026-07-03' AS Date), CAST(N'2026-07-03T09:17:24.0067679' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'khỏe', 0, NULL, NULL, CAST(N'2026-07-03T09:17:24.0067679' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (22, 1, CAST(N'2026-07-03' AS Date), CAST(N'2026-07-03T09:36:31.5570617' AS DateTime2), N'EVENING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'khỏe', 0, NULL, NULL, CAST(N'2026-07-03T09:36:31.5570617' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (23, 1, CAST(N'2026-07-03' AS Date), CAST(N'2026-07-03T09:57:01.1694442' AS DateTime2), N'RANDOM', 150, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'khỏe', 0, NULL, NULL, CAST(N'2026-07-03T09:57:01.1694442' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (24, 1, CAST(N'2026-07-03' AS Date), CAST(N'2026-07-03T10:10:31.1992714' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-03T10:10:31.1992714' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (25, 1, CAST(N'2026-07-03' AS Date), CAST(N'2026-07-03T10:27:19.6561954' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-03T10:27:19.6561954' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (26, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T10:17:47.5263209' AS DateTime2), N'MORNING', 130, 90, 80, NULL, N'MANUAL', NULL, 0, NULL, N'khỏe', 0, NULL, NULL, CAST(N'2026-07-12T10:17:47.5263209' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (27, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T10:19:35.3430571' AS DateTime2), N'MORNING', 120, 80, 200, NULL, N'MANUAL', NULL, 0, NULL, N'lo', 0, NULL, NULL, CAST(N'2026-07-12T10:19:35.3430571' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (28, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T10:20:57.5571686' AS DateTime2), N'MORNING', 130, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'', 0, NULL, NULL, CAST(N'2026-07-12T10:20:57.5571686' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (29, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T10:21:33.8160418' AS DateTime2), N'MORNING', 300, 200, 70, NULL, N'MANUAL', NULL, 0, NULL, N'4', 0, NULL, NULL, CAST(N'2026-07-12T10:21:33.8160418' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (30, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T10:34:39.7146943' AS DateTime2), N'MORNING', 120, 54, 44, NULL, N'MANUAL', NULL, 0, NULL, N'', 0, NULL, NULL, CAST(N'2026-07-12T10:34:39.7146943' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (31, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T10:34:53.8643052' AS DateTime2), N'MORNING', 50, 34, 34, NULL, N'MANUAL', NULL, 0, NULL, N'34', 0, NULL, NULL, CAST(N'2026-07-12T10:34:53.8643052' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (32, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T13:07:03.5530602' AS DateTime2), N'MORNING', 118, 78, 70, NULL, N'MANUAL', NULL, 0, NULL, N'', 0, NULL, NULL, CAST(N'2026-07-12T13:07:03.5530602' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (33, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T15:42:08.2516148' AS DateTime2), N'MORNING', 150, 90, 80, NULL, N'MANUAL', NULL, 0, NULL, N'1', 0, NULL, NULL, CAST(N'2026-07-12T15:42:08.2516148' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (34, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T15:42:17.1550202' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(10.00 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'1', 0, NULL, NULL, CAST(N'2026-07-12T15:42:17.1550202' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (35, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T15:45:24.5840479' AS DateTime2), N'MORNING', 140, 90, 80, NULL, N'MANUAL', NULL, 0, NULL, N'2', 0, NULL, NULL, CAST(N'2026-07-12T15:45:24.5840479' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (36, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T15:45:34.9829782' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(11.00 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'2', 0, NULL, NULL, CAST(N'2026-07-12T15:45:34.9829782' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (37, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T15:46:46.3244300' AS DateTime2), N'MORNING', 128, 80, 78, NULL, N'MANUAL', NULL, 0, NULL, N'3', 0, NULL, NULL, CAST(N'2026-07-12T15:46:46.3244300' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (38, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T15:47:01.7963263' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(8.00 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'2', 0, NULL, NULL, CAST(N'2026-07-12T15:47:01.7963263' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (39, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T15:47:23.8618688' AS DateTime2), N'EVENING', 60, 56, 80, NULL, N'MANUAL', NULL, 0, NULL, N'2', 0, NULL, NULL, CAST(N'2026-07-12T15:47:23.8618688' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (40, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T15:51:55.9920540' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(2.00 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'3', 0, NULL, NULL, CAST(N'2026-07-12T15:51:55.9920540' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (41, 1, CAST(N'2026-07-12' AS Date), CAST(N'2026-07-12T15:58:55.2858374' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(8.00 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'3', 0, NULL, NULL, CAST(N'2026-07-12T15:58:55.2858374' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (42, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T22:14:57.8090133' AS DateTime2), N'EVENING', 120, 80, 78, NULL, N'MANUAL', NULL, 0, NULL, N'khỏe', 0, NULL, NULL, CAST(N'2026-07-13T22:14:57.8090133' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (43, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T22:28:36.8918151' AS DateTime2), N'RANDOM', 122, 88, 77, NULL, N'MANUAL', NULL, 0, NULL, N'khỏe', 0, NULL, NULL, CAST(N'2026-07-13T22:28:36.8918151' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (44, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T22:32:47.8624314' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-13T22:32:47.8624314' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (45, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:08:49.4880240' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(6.50 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:08:49.4880240' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (46, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:42:27.2224310' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:42:27.2224310' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (47, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:42:30.7413452' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-13T23:42:30.7413452' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (48, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:43:20.3463714' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:43:20.3463714' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (49, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:43:24.2744474' AS DateTime2), N'EVENING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:43:24.2744474' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (50, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:43:29.1453998' AS DateTime2), N'RANDOM', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:43:29.1453998' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (51, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:43:32.1122522' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-13T23:43:32.1122522' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (52, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:43:41.8426397' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-13T23:43:41.8426397' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (53, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:43:52.3032388' AS DateTime2), N'RANDOM', NULL, NULL, NULL, CAST(6.60 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'02/07/2026 21:30', 0, NULL, NULL, CAST(N'2026-07-13T23:43:52.3032388' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (54, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:44:06.0192379' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:44:06.0192379' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (55, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:44:09.4137577' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-13T23:44:09.4137577' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (56, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:48:41.0190984' AS DateTime2), N'RANDOM', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:48:41.0190984' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (57, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:48:46.9584797' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-13T23:48:46.9584797' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (58, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:48:49.7085692' AS DateTime2), N'RANDOM', NULL, NULL, NULL, CAST(6.60 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'02/07/2026 21:30', 0, NULL, NULL, CAST(N'2026-07-13T23:48:49.7085692' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (59, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:49:08.1546751' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:49:08.1546751' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (60, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:49:13.2002183' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:49:13.2002183' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (61, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:49:15.9675027' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-13T23:49:15.9675027' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (62, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:49:18.7834066' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-13T23:49:18.7834066' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (63, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:49:23.0768381' AS DateTime2), N'EVENING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:49:23.0768381' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (64, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:49:55.3315134' AS DateTime2), N'RANDOM', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:49:55.3315134' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (65, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:50:04.6988967' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-13T23:50:04.6988967' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (66, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:50:07.8625633' AS DateTime2), N'RANDOM', NULL, NULL, NULL, CAST(6.60 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'02/07/2026 21:30', 0, NULL, NULL, CAST(N'2026-07-13T23:50:07.8625633' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (67, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:50:25.8208199' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:50:25.8208199' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (68, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:50:29.6030224' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:50:29.6030224' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (69, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:50:32.6509712' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-13T23:50:32.6509712' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (70, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:50:36.3164031' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-13T23:50:36.3164031' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (71, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:50:39.5046529' AS DateTime2), N'EVENING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:50:39.5046529' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (72, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:51:29.1181321' AS DateTime2), N'RANDOM', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:51:29.1181321' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (73, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:51:38.2082247' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-13T23:51:38.2082247' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (74, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:51:40.9394259' AS DateTime2), N'RANDOM', NULL, NULL, NULL, CAST(6.60 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'02/07/2026 21:30', 0, NULL, NULL, CAST(N'2026-07-13T23:51:40.9394259' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (75, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:52:02.1785984' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:52:02.1785984' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (76, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:52:05.9577401' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:52:05.9577401' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (77, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:52:08.3438694' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-13T23:52:08.3438694' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (78, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:52:11.0002958' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-13T23:52:11.0002958' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (79, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:52:14.0237346' AS DateTime2), N'EVENING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:52:14.0237346' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (80, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:53:13.9416382' AS DateTime2), N'EVENING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:53:13.9416382' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (81, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:53:18.5625951' AS DateTime2), N'RANDOM', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:53:18.5625951' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (82, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:53:24.6707881' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-13T23:53:24.6707881' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (83, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:53:27.3997501' AS DateTime2), N'RANDOM', NULL, NULL, NULL, CAST(6.60 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'02/07/2026 21:30', 0, NULL, NULL, CAST(N'2026-07-13T23:53:27.3997501' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (84, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:53:33.0326512' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-13T23:53:33.0326512' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (85, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:53:43.4098175' AS DateTime2), N'RANDOM', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:53:43.4098175' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (86, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:53:53.9055341' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:53:53.9055341' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (87, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:53:57.1464231' AS DateTime2), N'RANDOM', NULL, NULL, NULL, CAST(6.60 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'02/07/2026 21:30', 0, NULL, NULL, CAST(N'2026-07-13T23:53:57.1464231' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (88, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:54:00.5540147' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:54:00.5540147' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (89, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:54:03.1206075' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-13T23:54:03.1206075' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (90, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:54:06.1127178' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-13T23:54:06.1127178' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (91, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:54:10.2413982' AS DateTime2), N'EVENING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:54:10.2413982' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (92, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:55:42.8521803' AS DateTime2), N'EVENING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:55:42.8521803' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (93, 2, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:55:56.5414789' AS DateTime2), N'RANDOM', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:55:56.5414789' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (94, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:56:22.8293386' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-13T23:56:22.8293386' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (95, 3, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:56:35.3485379' AS DateTime2), N'RANDOM', NULL, NULL, NULL, CAST(6.60 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'02/07/2026 21:30', 0, NULL, NULL, CAST(N'2026-07-13T23:56:35.3485379' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (96, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:56:59.6500490' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-13T23:56:59.6500490' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (97, 1, CAST(N'2026-07-13' AS Date), CAST(N'2026-07-13T23:57:35.8287841' AS DateTime2), N'RANDOM', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-13T23:57:35.8287841' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (98, 1, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T00:00:27.9269147' AS DateTime2), N'EVENING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T00:00:27.9269147' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (99, 2, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T00:00:44.5891519' AS DateTime2), N'RANDOM', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T00:00:44.5891519' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (100, 3, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T00:01:17.5734547' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-14T00:01:17.5734547' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (101, 3, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T00:01:33.0795649' AS DateTime2), N'RANDOM', NULL, NULL, NULL, CAST(6.60 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'02/07/2026 21:30', 0, NULL, NULL, CAST(N'2026-07-14T00:01:33.0795649' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (102, 1, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T00:02:05.3949392' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-14T00:02:05.3949392' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (103, 1, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T00:02:52.6544727' AS DateTime2), N'RANDOM', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T00:02:52.6544727' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (104, 2, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T00:03:44.7547027' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T00:03:44.7547027' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (105, 1, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T00:04:01.7262454' AS DateTime2), N'RANDOM', NULL, NULL, NULL, CAST(6.60 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'02/07/2026 21:30', 0, NULL, NULL, CAST(N'2026-07-14T00:04:01.7262454' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (106, 1, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T08:41:25.3404173' AS DateTime2), N'EVENING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T08:41:25.3404173' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (107, 2, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T08:41:39.8887547' AS DateTime2), N'RANDOM', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T08:41:39.8887547' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (108, 3, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T08:42:10.7692842' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-14T08:42:10.7692842' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (109, 3, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T08:42:24.3694742' AS DateTime2), N'RANDOM', NULL, NULL, NULL, CAST(6.60 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'02/07/2026 21:30', 0, NULL, NULL, CAST(N'2026-07-14T08:42:24.3694742' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (110, 1, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T08:42:53.1814073' AS DateTime2), N'EVENING', NULL, NULL, NULL, CAST(7.20 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 20h', 0, NULL, NULL, CAST(N'2026-07-14T08:42:53.1814073' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (111, 1, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T08:43:37.8610168' AS DateTime2), N'RANDOM', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T08:43:37.8610168' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (112, 2, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T08:44:26.1233798' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T08:44:26.1233798' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (113, 1, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T08:44:42.3459524' AS DateTime2), N'RANDOM', NULL, NULL, NULL, CAST(6.60 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'02/07/2026 21:30', 0, NULL, NULL, CAST(N'2026-07-14T08:44:42.3459524' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (114, 1, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T08:44:59.4144499' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T08:44:59.4144499' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (115, 3, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T08:45:12.2486564' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-14T08:45:12.2486564' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (116, 1, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T08:45:28.5420447' AS DateTime2), N'MORNING', NULL, NULL, NULL, CAST(5.80 AS Decimal(4, 2)), N'MANUAL', NULL, 0, NULL, N'Đo lúc 6h30', 0, NULL, NULL, CAST(N'2026-07-14T08:45:28.5420447' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (117, 2, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T08:45:44.1750990' AS DateTime2), N'EVENING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T08:45:44.1750990' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (118, 2, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T09:17:30.3633751' AS DateTime2), N'MORNING', 120, 80, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T09:17:30.3633751' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (119, 2, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T09:19:08.0860242' AS DateTime2), N'MORNING', 80, 150, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T09:19:08.0860242' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (120, 2, CAST(N'2026-07-14' AS Date), CAST(N'2026-07-14T09:22:29.9989103' AS DateTime2), N'MORNING', 80, 150, 75, NULL, N'MANUAL', NULL, 0, NULL, N'Khỏe', 0, NULL, NULL, CAST(N'2026-07-14T09:22:29.9989103' AS DateTime2))
GO
INSERT [dbo].[daily_health_logs] ([id], [patient_id], [log_date], [log_time], [log_type], [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], [input_method], [image_url], [is_ocr_validated], [alert_level], [patient_notes], [is_alert_processed], [alert_processed_at], [alert_processed_by], [created_at]) VALUES (121, 1, CAST(N'2026-07-20' AS Date), CAST(N'2026-07-20T09:09:32.9855065' AS DateTime2), N'MORNING', 122, 88, 88, NULL, N'MANUAL', NULL, 0, NULL, N'khoe', 0, NULL, NULL, CAST(N'2026-07-20T09:09:32.9855065' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[daily_health_logs] OFF
GO
SET IDENTITY_INSERT [dbo].[diet_logs] ON 
GO
INSERT [dbo].[diet_logs] ([id], [patient_id], [food_id], [log_date], [meal_type], [quantity_g], [logged_at]) VALUES (3, 1, 5, CAST(N'2026-06-28' AS Date), N'BREAKFAST', CAST(100.00 AS Decimal(6, 2)), CAST(N'2026-06-28T22:50:02.5999268' AS DateTime2))
GO
INSERT [dbo].[diet_logs] ([id], [patient_id], [food_id], [log_date], [meal_type], [quantity_g], [logged_at]) VALUES (4, 1, 8, CAST(N'2026-06-28' AS Date), N'LUNCH', CAST(100.00 AS Decimal(6, 2)), CAST(N'2026-06-28T22:50:12.9766261' AS DateTime2))
GO
INSERT [dbo].[diet_logs] ([id], [patient_id], [food_id], [log_date], [meal_type], [quantity_g], [logged_at]) VALUES (5, 1, 16, CAST(N'2026-06-28' AS Date), N'BREAKFAST', CAST(100.00 AS Decimal(6, 2)), CAST(N'2026-06-28T22:50:22.1060179' AS DateTime2))
GO
INSERT [dbo].[diet_logs] ([id], [patient_id], [food_id], [log_date], [meal_type], [quantity_g], [logged_at]) VALUES (6, 1, 8, CAST(N'2026-07-12' AS Date), N'BREAKFAST', CAST(100.00 AS Decimal(6, 2)), CAST(N'2026-07-12T18:05:40.9189221' AS DateTime2))
GO
INSERT [dbo].[diet_logs] ([id], [patient_id], [food_id], [log_date], [meal_type], [quantity_g], [logged_at]) VALUES (7, 1, 7, CAST(N'2026-07-20' AS Date), N'DINNER', CAST(97.00 AS Decimal(6, 2)), CAST(N'2026-07-20T09:18:22.3059584' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[diet_logs] OFF
GO
SET IDENTITY_INSERT [dbo].[disease_profiles] ON 
GO
INSERT [dbo].[disease_profiles] ([id], [profile_code], [profile_name], [description], [requires_bp_input], [requires_glucose_input], [is_active]) VALUES (1, N'HYPERTENSION', N'Tăng huyết áp', N'Bệnh nhân chỉ theo dõi huyết áp và nhịp tim. Màn hình ẩn phần nhập tiểu đường.', 1, 0, 1)
GO
INSERT [dbo].[disease_profiles] ([id], [profile_code], [profile_name], [description], [requires_bp_input], [requires_glucose_input], [is_active]) VALUES (2, N'DIABETES', N'Tiểu đường Type 2', N'Bệnh nhân chỉ theo dõi glucose. Màn hình ẩn phần nhập huyết áp.', 0, 1, 1)
GO
INSERT [dbo].[disease_profiles] ([id], [profile_code], [profile_name], [description], [requires_bp_input], [requires_glucose_input], [is_active]) VALUES (3, N'BOTH', N'Đồng mắc (Cả hai bệnh)', N'Bệnh nhân bắt buộc phải nhập đầy đủ cả hai nhóm chỉ số huyết áp và glucose.', 1, 1, 1)
GO
SET IDENTITY_INSERT [dbo].[disease_profiles] OFF
GO
SET IDENTITY_INSERT [dbo].[doctors] ON 
GO
INSERT [dbo].[doctors] ([id], [account_id], [hospital_id], [doctor_code], [full_name], [gender], [phone], [specialty], [capacity_limit], [current_patient_count], [is_active], [created_at], [updated_at]) VALUES (5, 10, 1, N'DOC-001', N'Bác sĩ Nguyễn Văn A', N'MALE', N'0901111111', N'Tim mạch', 50, 0, 1, CAST(N'2026-06-27T18:44:22.7333333' AS DateTime2), CAST(N'2026-06-27T18:44:22.7362140' AS DateTime2))
GO
INSERT [dbo].[doctors] ([id], [account_id], [hospital_id], [doctor_code], [full_name], [gender], [phone], [specialty], [capacity_limit], [current_patient_count], [is_active], [created_at], [updated_at]) VALUES (6, 11, 1, N'DOC-002', N'Bác sĩ Lê Thị B', N'FEMALE', N'0902222222', N'Cả tiểu đường và huyết áp', 50, 0, 1, CAST(N'2026-06-27T18:44:22.7366667' AS DateTime2), CAST(N'2026-06-27T18:44:22.7372164' AS DateTime2))
GO
INSERT [dbo].[doctors] ([id], [account_id], [hospital_id], [doctor_code], [full_name], [gender], [phone], [specialty], [capacity_limit], [current_patient_count], [is_active], [created_at], [updated_at]) VALUES (7, 12, 1, N'DOC-003', N'Đỗ Mạnh Toàn', N'MALE', N'012345678', N'Tiểu đường', 50, 3, 1, CAST(N'2026-06-27T18:50:28.2804097' AS DateTime2), CAST(N'2026-07-13T22:44:31.9226994' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[doctors] OFF
GO
SET IDENTITY_INSERT [dbo].[emergency_guides] ON 
GO
INSERT [dbo].[emergency_guides] ([id], [hospital_id], [alert_level], [metric_type], [title], [instruction_content], [is_active], [created_at], [updated_at]) VALUES (1, 1, N'RED', N'BLOOD_PRESSURE', N'Xử trí khi huyết áp tăng cao đột ngột', N'Bước 1. Nghỉ ngơi tại chỗ
- Cho người bệnh ngồi hoặc nằm yên, hít thở sâu, giữ bình tĩnh.
- Nới lỏng quần áo, tránh đám đông vây quanh.
- Nếu nằm: kê đầu cao khoảng 30°, không kê chân cao hơn đầu.
- Nếu khó thở: cho ngồi dậy, kê gối sau lưng.
- Nếu nôn: cho nằm nghiêng để tránh sặc.
- Không cho ăn, uống nếu có méo miệng, khó nói hoặc nghi ngờ đột quỵ.
Bước 2. Đo huyết áp
- Đo huyết áp để đánh giá tình trạng.
- Nếu lần đo đầu ≥180/120 mmHg, cho người bệnh tiếp tục nghỉ ngơi và đo lại sau 15 phút.
Bước 3. Xử trí theo kết quả đo
- Nếu huyết áp vẫn ≥180/120 mmHg nhưng không có triệu chứng tổn thương cơ quan đích: uống thuốc hạ huyết áp theo chỉ định, không dùng thuốc hạ áp nhanh (ví dụ: Nifedipin ngậm dưới lưỡi), sau đó đến cơ sở y tế để được bác sĩ điều chỉnh điều trị.
- Nếu huyết áp ≥180/120 mmHg kèm triệu chứng tổn thương cơ quan đích: gọi cấp cứu hoặc đưa ngay người bệnh đến bệnh viện/cơ sở y tế gần nhất.', 1, CAST(N'2026-06-27T11:31:39.1196406' AS DateTime2), CAST(N'2026-07-12T16:18:34.3140765' AS DateTime2))
GO
INSERT [dbo].[emergency_guides] ([id], [hospital_id], [alert_level], [metric_type], [title], [instruction_content], [is_active], [created_at], [updated_at]) VALUES (2, 1, N'RED', N'GLUCOSE', N'Hướng dẫn khẩn cấp khi hạ đường huyết', N'Bước 1. Ngừng thuốc
- Ngừng ngay thuốc hạ đường huyết hoặc insulin.
Bước 2. Xử trí
- Nếu còn tỉnh táo:
- Uống ngay nước đường hoặc đồ uống có đường.
- Sau đó ăn thêm cháo, sữa, hoa quả hoặc bánh ngọt.
- Nếu hôn mê hoặc mất ý thức:
- Không cho ăn hoặc uống.
- Đưa ngay đến cơ sở y tế để được tiêm tĩnh mạch dung dịch glucose và truyền glucose theo chỉ định.', 1, CAST(N'2026-06-27T11:31:39.1196406' AS DateTime2), CAST(N'2026-07-12T16:31:34.6415174' AS DateTime2))
GO
INSERT [dbo].[emergency_guides] ([id], [hospital_id], [alert_level], [metric_type], [title], [instruction_content], [is_active], [created_at], [updated_at]) VALUES (3, 1, N'RED', N'GLUCOSE', N'Hướng dẫn khẩn cấp khi tăng đường huyết', N'Bước 1. Liên hệ bác sĩ hoặc đến cơ sở y tế
- Khi đường huyết tăng cao, người bệnh nên liên hệ ngay bác sĩ hoặc đến cơ sở y tế gần nhất để được xử trí kịp thời.
Bước 2. Hỗ trợ trong khi chờ được xử trí
- Uống nhiều nước (không áp dụng với người bệnh thận nặng hoặc suy tim nặng chưa được kiểm soát).
- Nếu đang điều trị insulin: có thể tiêm thêm 1–2 đơn vị insulin (so với liều thường dùng) sau khi tham khảo ý kiến bác sĩ.
- Vận động nhẹ 15–20 phút nếu còn tỉnh táo, không chóng mặt hoặc sốt.
- Lưu ý: Chỉ áp dụng các biện pháp trên khi đường huyết tăng do thay đổi chế độ ăn uống hoặc sinh hoạt, không áp dụng khi người bệnh quên uống thuốc.
Bước 3. Gọi cấp cứu ngay nếu
- Có bệnh lý nghiêm trọng hoặc dấu hiệu như: yếu/liệt chi, lờ đờ, mất ý thức, không thể ăn uống.', 1, CAST(N'2026-07-12T16:31:34.7720577' AS DateTime2), CAST(N'2026-07-12T16:31:34.7720577' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[emergency_guides] OFF
GO
SET IDENTITY_INSERT [dbo].[emergency_protocols] ON 
GO
INSERT [dbo].[emergency_protocols] ([id], [hospital_id], [condition_type], [title], [warning_signs], [instruction_content], [is_active], [created_at], [updated_at]) VALUES (1, 1, N'HYPERTENSIVE_CRISIS', N'Cẩm nang Huyết áp', N'Tăng huyết áp đột ngột thường xảy ra khi Huyết áp tâm thu ≥180 mmHg hoặc Huyết áp tâm trương ≥120 mmHg. Các triệu chứng có thể gặp:
- Đau đầu dữ dội.
- Chóng mặt, choáng váng.
- Mờ mắt.
- Khó nói.
- Đau tức ngực.
- Tim đập nhanh.
- Khó thở.
- Buồn nôn hoặc nôn.
- Chảy máu cam.
- Tê yếu tay chân.
- Đi lại không vững.
- Miệng méo.
- Liệt mặt.
- Co giật.
- Lơ mơ hoặc hôn mê.', N'I. Huyết áp là gì?
Huyết áp là áp lực của máu tác động lên thành động mạch khi tim bơm máu đi nuôi cơ thể.
Kết quả đo được ghi theo đơn vị mmHg dưới dạng:
Huyết áp tâm thu / Huyết áp tâm trương
Ví dụ: 120/80 mmHg
- Huyết áp tâm thu: Chỉ số phía trên, phản ánh áp lực khi tim co bóp.
- Huyết áp tâm trương: Chỉ số phía dưới, phản ánh áp lực khi tim nghỉ giữa hai nhịp đập.

II. Phân loại huyết áp theo màu cảnh báo
🟩 Xanh lá (An toàn - Đạt mục tiêu):
Tâm thu <120 mmHg và tâm trương <80 mmHg. Huyết áp trong giới hạn mục tiêu. Tiếp tục duy trì lối sống lành mạnh và theo dõi định kỳ.
🟨 Vàng (Chú ý - Level 1):
Tâm thu 130–139 mmHg hoặc tâm trương 85–89 mmHg. Huyết áp bắt đầu tăng. Cần theo dõi thường xuyên và điều chỉnh chế độ ăn uống, sinh hoạt.
🟧 Cam (Nguy hiểm - Level 2):
• Tâm thu 140–179 mmHg hoặc tâm trương 90–109 mmHg.
• Hoặc huyết áp <90/60 mmHg (hạ huyết áp).
Huyết áp bất thường, có nguy cơ ảnh hưởng đến sức khỏe. Nên theo dõi sát và tham khảo ý kiến bác sĩ.
🟥 Đỏ (Cấp cứu - Level 3):
Tâm thu ≥180 mmHg hoặc tâm trương ≥110 mmHg. Huyết áp rất cao. Nếu kèm triệu chứng nguy hiểm cần cấp cứu ngay.
Nguyên tắc phân loại:
Chỉ cần một trong hai chỉ số thuộc mức cao hơn thì xếp theo mức cảnh báo cao hơn. Ví dụ:
- 118/75: Xanh
- 135/78: Vàng
- 118/95: Cam
- 145/88: Cam
- 185/95: Đỏ
- 85/55: Cam (Hạ huyết áp)

III. Hướng dẫn đo huyết áp đúng cách
1. Chuẩn bị trước khi đo
- Nghỉ ngơi trong phòng yên tĩnh từ 5–10 phút.
- Không uống cà phê, rượu bia hoặc hút thuốc trong vòng 2 giờ trước khi đo.
- Không vận động mạnh trước khi đo.
2. Tư thế đo
- Ngồi trên ghế có tựa lưng. Thả lỏng cơ thể.
- Hai chân đặt trên sàn, không bắt chéo chân.
- Cánh tay đặt trên bàn ngang mức tim.
- Ngoài tư thế ngồi, có thể đo ở tư thế nằm hoặc đứng. Người cao tuổi hoặc mắc đái tháo đường nên đo thêm huyết áp khi đứng để phát hiện hạ huyết áp tư thế.
3. Thiết bị đo
- Có thể sử dụng: Máy đo huyết áp thủy ngân, Máy đo huyết áp đồng hồ hoặc Máy đo huyết áp điện tử đo ở cánh tay.
- Máy đo cần được kiểm chuẩn định kỳ và sử dụng bao quấn đúng kích thước.
4. Trong khi đo
- Không nói chuyện.
- Nếu đo bằng máy cơ: Xác định động mạch cánh tay. Bơm hơi cao hơn mức mất mạch khoảng 30 mmHg. Xả hơi với tốc độ 2–3 mmHg mỗi nhịp đập. Huyết áp tâm thu là thời điểm xuất hiện tiếng Korotkoff đầu tiên. Huyết áp tâm trương là thời điểm mất hẳn tiếng Korotkoff.
5. Số lần đo
- Lần đầu nên đo ở cả hai tay. Tay nào có huyết áp cao hơn sẽ dùng để theo dõi về sau.
- Đo ít nhất 2 lần, mỗi lần cách nhau 1–2 phút. Nếu hai lần đo chênh nhau trên 10 mmHg, nghỉ trên 5 phút rồi đo lại. Giá trị huyết áp ghi nhận là trung bình của hai lần đo cuối cùng.
6. Ghi kết quả
- Ghi theo dạng: Huyết áp tâm thu / Huyết áp tâm trương (mmHg). Ví dụ: 126/82 mmHg. Không làm tròn quá hàng đơn vị.

V. Cách xử trí khi huyết áp tăng cao đột ngột
Bước 1. Cho người bệnh nghỉ ngơi
- Nằm hoặc ngồi yên. Hít thở sâu, giữ bình tĩnh.
- Nới lỏng quần áo. Tránh để nhiều người vây quanh.
- Nếu nằm: Kê đầu cao khoảng 30°. Không kê chân cao hơn đầu.
- Nếu khó thở: Ngồi tựa lưng.
- Nếu nôn: Cho người bệnh nằm nghiêng để tránh sặc.
- Nếu có dấu hiệu méo miệng, khó nói hoặc nghi ngờ đột quỵ: Không cho ăn hoặc uống.
Bước 2. Đo huyết áp
- Đo huyết áp để đánh giá tình trạng người bệnh.
- Trường hợp 1: Huyết áp ≥180/120 mmHg nhưng không có triệu chứng nguy hiểm (không có đau ngực, khó thở, mờ mắt, khó nói, liệt, co giật, tiểu máu, nôn nhiều): Nghỉ ngơi. Đo lại sau 15 phút. Nếu lần đo thứ hai vẫn cao: Đây là cơn tăng huyết áp khẩn trương. Dùng thuốc hạ huyết áp theo chỉ định của bác sĩ để hạ huyết áp từ từ trong 28–48 giờ. Không sử dụng thuốc hạ huyết áp tác dụng nhanh (ví dụ: Nifedipin ngậm dưới lưỡi). Đến cơ sở y tế để được bác sĩ điều chỉnh thuốc điều trị.
- Trường hợp 2: Huyết áp ≥180/120 mmHg và có triệu chứng nguy hiểm (có đau ngực, khó thở, đau lưng dữ dội, yếu hoặc liệt nửa người, khó nói, mờ mắt, co giật, tiểu máu, nôn nhiều, lơ mơ hoặc hôn mê): → Đây là cơn tăng huyết áp cấp cứu. Cần gọi cấp cứu hoặc đưa người bệnh đến bệnh viện gần nhất ngay lập tức.

VI. Những điều không nên làm
- Không nói chuyện trong khi đo huyết áp.
- Không uống cà phê, rượu bia hoặc hút thuốc trước khi đo.
- Không tự ý dùng thuốc hạ huyết áp tác dụng nhanh khi chưa có chỉ định của bác sĩ.
- Không để người bệnh đi lại nhiều khi huyết áp tăng cao.
- Không cho người bệnh ăn hoặc uống nếu có dấu hiệu nghi ngờ đột quỵ.', 1, CAST(N'2026-06-27T11:31:39.1266476' AS DateTime2), CAST(N'2026-07-12T17:26:54.5882043' AS DateTime2))
GO
INSERT [dbo].[emergency_protocols] ([id], [hospital_id], [condition_type], [title], [warning_signs], [instruction_content], [is_active], [created_at], [updated_at]) VALUES (2, 1, N'HYPOGLYCEMIA', N'Cẩm nang Đường huyết', N'Hạ đường huyết (<4,4 mmol/L):
Run tay, đói cồn cào, tim đập nhanh, vã mồ hôi, hoa mắt, chóng mặt, mệt lả. Đi lại khó khăn, nhìn mờ, co giật, hôn mê.
Tăng đường huyết (>10 mmol/L):
Mệt mỏi, khát nước nhiều, tiểu nhiều, tiểu đêm, mờ mắt, tê bì tay chân, mất nước, yếu nhiều, lơ mơ, hôn mê.', N'I. Đường huyết là gì?
Đường huyết là nồng độ glucose trong máu, cung cấp năng lượng cho các tế bào, đặc biệt là não và hệ thần kinh.
Nếu đường huyết quá cao hoặc quá thấp đều có thể gây ảnh hưởng nghiêm trọng đến sức khỏe, đặc biệt ở người mắc bệnh đái tháo đường.

II. Phân loại đường huyết theo màu cảnh báo
🔴 Level 3 - Hạ đường huyết:
< 4,4 mmol/L. Đường huyết thấp, cần xử trí sớm để tránh hôn mê hoặc co giật.
🟢 Bình thường:
4,4 – 10 mmol/L. Đường huyết trong giới hạn mục tiêu. Tiếp tục duy trì chế độ điều trị và theo dõi định kỳ.
🟠 Level 2 - Tăng đường huyết:
10 – 16 mmol/L. Đường huyết tăng cao, cần theo dõi và thực hiện các biện pháp kiểm soát theo hướng dẫn của bác sĩ.
🔴 Level 3 - Khẩn cấp:
> 16 mmol/L. Đường huyết rất cao, có nguy cơ xảy ra các biến chứng cấp tính. Cần liên hệ cơ sở y tế hoặc bác sĩ để được xử trí.

III. Dấu hiệu tăng đường huyết
Tăng đường huyết nhẹ:
Thông thường không có triệu chứng rõ ràng.
Đường huyết tăng cao:
Mệt mỏi, khát nước nhiều, tiểu nhiều, tiểu đêm, mờ mắt, tê bì tay chân.
Đường huyết rất cao:
Mất nước, yếu nhiều, lơ mơ, hôn mê nhiễm toan ceton, hôn mê tăng áp lực thẩm thấu.

IV. Nguyên nhân tăng đường huyết
- Chưa được chẩn đoán hoặc điều trị đái tháo đường.
- Không tuân thủ thuốc điều trị.
- Ăn uống không hợp lý.
- Ít vận động.
- Sử dụng thuốc làm tăng đường huyết (ví dụ: Corticoid).
- Nhiễm trùng, phẫu thuật, chấn thương hoặc các tình trạng gây stress cho cơ thể.

V. Khi nào cần gặp bác sĩ?
Người bệnh nên liên hệ bác sĩ khi:
- Tiêu chảy hoặc nôn kéo dài.
- Sốt trên 24 giờ.
- Đường huyết >13 mmol/L (240 mg/dL) dù đã dùng thuốc.
- Khó kiểm soát đường huyết.

VI. Khi nào cần cấp cứu?
Cần đến bệnh viện hoặc gọi cấp cứu ngay khi:
- Đường huyết duy trì >13 mmol/L và có ceton trong nước tiểu.
- Lơ mơ hoặc mất ý thức.
- Không ăn uống được.
- Có dấu hiệu tai biến mạch máu não, đau tim, yếu hoặc liệt tay chân, co giật.

VII. Xử trí khi tăng đường huyết tại nhà
Nếu đường huyết tăng do ăn uống hoặc sinh hoạt và người bệnh vẫn tỉnh táo:
1. Uống nhiều nước:
- Giúp giảm mất nước và hỗ trợ giảm đường huyết.
- Không áp dụng cho người suy tim nặng hoặc bệnh thận nặng nếu chưa có hướng dẫn của bác sĩ.
2. Tiêm thêm insulin:
- Chỉ thực hiện khi đang điều trị bằng insulin, có chỉ định hoặc hướng dẫn của bác sĩ. Không tự ý tăng liều insulin.
3. Vận động nhẹ:
- Đi bộ hoặc vận động nhẹ khoảng 15–20 phút nếu tỉnh táo, không sốt, không chóng mặt.
- Không nên tập luyện nếu cảm thấy mệt nhiều hoặc choáng váng.

VIII. Dấu hiệu hạ đường huyết
Khi đường huyết giảm thấp, người bệnh có thể gặp: Run tay, đói cồn cào, tim đập nhanh, vã mồ hôi, hoa mắt, chóng mặt, mệt lả. Nếu nặng hơn: đi lại khó khăn, nhìn mờ, co giật, hôn mê.

IX. Cách xử trí khi hạ đường huyết
Người bệnh còn tỉnh:
- Ngừng sử dụng thuốc hạ đường huyết hoặc insulin theo hướng dẫn của bác sĩ.
- Uống ngay nước đường hoặc đồ uống có đường.
- Sau đó ăn thêm: Cháo, Bánh, Sữa, Hoa quả.
Người bệnh hôn mê:
- Không cho ăn hoặc uống vì dễ gây sặc.
- Đưa đến cơ sở y tế ngay để được truyền glucose tĩnh mạch theo chỉ định của bác sĩ.

X. Phòng ngừa tăng và hạ đường huyết
- Tuân thủ điều trị: Dùng thuốc đúng chỉ định. Không tự ý bỏ thuốc. Không tự ý tăng hoặc giảm liều.
- Theo dõi đường huyết: Đo đường huyết theo hướng dẫn của bác sĩ. Khám sức khỏe định kỳ.
- Chế độ ăn: Ăn đủ các nhóm chất. Tăng rau xanh và chất xơ. Hạn chế đồ ngọt, thực phẩm chế biến sẵn. Chia nhỏ các bữa ăn.
- Hoạt động thể lực: Nên tập luyện khoảng 150 phút/tuần, kết hợp bài tập tăng sức mạnh 2–3 buổi/tuần.
- Kiểm soát cân nặng: Nếu thừa cân hoặc béo phì: Mục tiêu giảm 5–10% cân nặng trong 3–6 tháng.

XI. Ghi nhớ nhanh
- <4,4 mmol/L (Hạ đường huyết): Uống ngay nước đường nếu còn tỉnh; đến cơ sở y tế nếu nặng hoặc mất ý thức.
- 4,4–10 mmol/L (Bình thường): Duy trì chế độ điều trị, dinh dưỡng và vận động.
- 10–16 mmol/L (Tăng đường huyết): Uống đủ nước, theo dõi đường huyết, vận động nhẹ nếu phù hợp và liên hệ bác sĩ khi cần.
- >16 mmol/L (Khẩn cấp): Nguy cơ biến chứng cấp. Liên hệ bác sĩ hoặc đến cơ sở y tế ngay.', 1, CAST(N'2026-06-27T11:31:39.1266476' AS DateTime2), CAST(N'2026-07-12T17:26:54.6587108' AS DateTime2))
GO
INSERT [dbo].[emergency_protocols] ([id], [hospital_id], [condition_type], [title], [warning_signs], [instruction_content], [is_active], [created_at], [updated_at]) VALUES (3, 1, N'HYPERGLYCEMIA', N'Cẩm nang Tăng đường huyết (> 10.0 mmol/L)', N'Tăng đường huyết nhẹ: Thông thường không có triệu chứng rõ ràng.
Đường huyết tăng cao: Mệt mỏi, khát nước nhiều, tiểu nhiều, tiểu đêm, mờ mắt, tê bì tay chân.
Đường huyết rất cao: Mất nước, yếu nhiều, lơ mơ, hôn mê nhiễm toan ceton, hôn mê tăng áp lực thẩm thấu.', N'Khi nào cần gặp bác sĩ?
Người bệnh nên liên hệ bác sĩ khi:
- Tiêu chảy hoặc nôn kéo dài.
- Sốt trên 24 giờ.
- Đường huyết >13 mmol/L (240 mg/dL) dù đã dùng thuốc.
- Khó kiểm soát đường huyết.

Khi nào cần cấp cứu?
Cần đến bệnh viện hoặc gọi cấp cứu ngay khi:
- Đường huyết duy trì >13 mmol/L và có ceton trong nước tiểu.
- Lơ mơ hoặc mất ý thức.
- Không ăn uống được.
- Có dấu hiệu tai biến mạch máu não.
- Đau tim.
- Yếu hoặc liệt tay chân.
- Co giật.

Xử trí khi tăng đường huyết tại nhà (nếu đường huyết tăng do ăn uống hoặc sinh hoạt và người bệnh vẫn tỉnh táo):
1. Uống nhiều nước: Giúp giảm mất nước và hỗ trợ giảm đường huyết. Không áp dụng cho người suy tim nặng hoặc bệnh thận nặng nếu chưa có hướng dẫn của bác sĩ.
2. Tiêm thêm insulin: Chỉ thực hiện khi đang điều trị bằng insulin, có chỉ định hoặc hướng dẫn của bác sĩ. Không tự ý tăng liều insulin.
3. Vận động nhẹ: Nếu người bệnh tỉnh táo, không sốt, không chóng mặt, có thể đi bộ hoặc vận động nhẹ khoảng 15–20 phút. Không nên tập luyện nếu cảm thấy mệt nhiều hoặc choáng váng.', 0, CAST(N'2026-06-27T11:31:39.1266476' AS DateTime2), CAST(N'2026-07-12T17:26:54.6722452' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[emergency_protocols] OFF
GO
SET IDENTITY_INSERT [dbo].[exercise_logs] ON 
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (1, 1, CAST(N'2026-06-28' AS Date), N'Đi bộ', 30, 3000, CAST(N'2026-06-28T20:51:11.2171968' AS DateTime2), NULL)
GO
INSERT [dbo].[exercise_logs] ([id], [patient_id], [log_date], [exercise_type], [duration_minutes], [steps_count], [logged_at], [calories_burned]) VALUES (2, 1, CAST(N'2026-06-28' AS Date), N'Đi bộ', 30, 3000, CAST(N'2026-06-28T20:51:15.5408765' AS DateTime2), NULL)
GO
SET IDENTITY_INSERT [dbo].[exercise_logs] OFF
GO
SET IDENTITY_INSERT [dbo].[foods_dictionary] ON 
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (5, N'1001', N'Gạo nếp cái', N'Glutinous rice, milled', CAST(14.00 AS Decimal(5, 2)), 344, CAST(8.60 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(74.50 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2600832' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (6, N'1002', N'Gạo nếp máy (Loại thường)', N'Glutinous rice, milled', CAST(13.90 AS Decimal(5, 2)), 346, CAST(8.40 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(74.90 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2610944' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (7, N'1003', N'Gạo tẻ giã', N'Under milled, home-pounded rice', CAST(14.00 AS Decimal(5, 2)), 344, CAST(8.10 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), CAST(75.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2610944' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (8, N'1004', N'Gạo tẻ máy', N'Ordinary polished rice', CAST(14.00 AS Decimal(5, 2)), 344, CAST(7.90 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(75.90 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2610944' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (9, N'1005', N'Gạo lứt', N'Rice, brown or hulled', CAST(12.40 AS Decimal(5, 2)), 345, CAST(7.50 AS Decimal(5, 2)), CAST(2.70 AS Decimal(5, 2)), CAST(72.80 AS Decimal(5, 2)), CAST(3.40 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2620935' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (10, N'1006', N'Kê', N'Foxtail millet', CAST(14.00 AS Decimal(5, 2)), 331, CAST(7.00 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), CAST(69.00 AS Decimal(5, 2)), CAST(3.40 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2620935' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (11, N'1007', N'Ngô bắp tươi', N'Fresh maize seeds, raw', CAST(52.00 AS Decimal(5, 2)), 196, CAST(4.10 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), CAST(39.60 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2620935' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (12, N'1008', N'Ngô vàng hạt khô', N'Yellow maize, dried seeds', CAST(14.00 AS Decimal(5, 2)), 354, CAST(8.60 AS Decimal(5, 2)), CAST(4.70 AS Decimal(5, 2)), CAST(69.40 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2620935' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (13, N'1009', N'Bánh bao nhân thịt', N'Ball shaped dumpling, Vietnamese steamed buns', CAST(44.00 AS Decimal(5, 2)), 219, CAST(6.10 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(47.50 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2620935' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (14, N'1010', N'Bánh đa nem', N'Rice paper for rollers', CAST(15.00 AS Decimal(5, 2)), 333, CAST(4.00 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(78.90 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2620935' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (15, N'1011', N'Bánh đúc', N'Rice pudding', CAST(86.80 AS Decimal(5, 2)), 52, CAST(0.90 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(11.30 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2630821' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (16, N'1012', N'Bánh mì', N'French bread', CAST(37.20 AS Decimal(5, 2)), 249, CAST(7.90 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(52.60 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2630821' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (17, N'1013', N'Bánh phở', N'Rice noodles', CAST(64.30 AS Decimal(5, 2)), 143, CAST(3.20 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(31.70 AS Decimal(5, 2)), NULL, CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2630821' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (18, N'1014', N'Bánh quẩy', N'Wheat fritters twisted fried', CAST(38.70 AS Decimal(5, 2)), 292, CAST(8.00 AS Decimal(5, 2)), CAST(10.80 AS Decimal(5, 2)), CAST(40.70 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2630821' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (19, N'1015', N'Bỏng ngô', N'Corn flakes', CAST(3.00 AS Decimal(5, 2)), 372, CAST(8.60 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(80.80 AS Decimal(5, 2)), CAST(3.40 AS Decimal(5, 2)), CAST(2.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2630821' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (20, N'1016', N'Bột gạo nếp', N'Glutinous rice flour', CAST(10.00 AS Decimal(5, 2)), 362, CAST(8.20 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(78.80 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2630821' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (21, N'1017', N'Bột gạo tẻ', N'Rice ordinary flour', CAST(10.00 AS Decimal(5, 2)), 359, CAST(6.60 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(82.20 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2640962' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (22, N'1018', N'Bột mì', N'Wheat flour', CAST(14.00 AS Decimal(5, 2)), 346, CAST(10.30 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(73.60 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2640962' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (23, N'1019', N'Bột ngô vàng', N'Yellow maize flour', CAST(12.00 AS Decimal(5, 2)), 361, CAST(8.30 AS Decimal(5, 2)), CAST(4.00 AS Decimal(5, 2)), CAST(73.00 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (24, N'1020', N'Bún', N'Rice vermicelli', CAST(72.00 AS Decimal(5, 2)), 110, CAST(1.70 AS Decimal(5, 2)), NULL, CAST(25.70 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (25, N'1021', N'Cốm', N'Rice, unriped. Raw', CAST(25.00 AS Decimal(5, 2)), 297, CAST(6.10 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(66.30 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (26, N'1022', N'Mì sợi', N'Wheat noodles', CAST(13.00 AS Decimal(5, 2)), 349, CAST(11.00 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), CAST(74.20 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (27, N'1023', N'Ngô nếp luộc', N'Glutinous maize, boiled', CAST(59.00 AS Decimal(5, 2)), 167, CAST(3.90 AS Decimal(5, 2)), CAST(2.20 AS Decimal(5, 2)), CAST(32.90 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (28, N'2001', N'Củ ấu', N'Water-caltrops', CAST(69.20 AS Decimal(5, 2)), 115, CAST(3.60 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(24.00 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(1.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (29, N'2002', N'Củ cái', N'Yam winged', CAST(65.50 AS Decimal(5, 2)), 127, CAST(3.10 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(28.30 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (30, N'2003', N'Củ dong', N'Bermuda tuber', CAST(66.50 AS Decimal(5, 2)), 119, CAST(1.40 AS Decimal(5, 2)), NULL, CAST(28.40 AS Decimal(5, 2)), CAST(2.40 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (31, N'2004', N'Củ sắn', N'Bitter cassava', CAST(60.00 AS Decimal(5, 2)), 152, CAST(1.10 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(36.40 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (32, N'2005', N'Củ sắn dây', N'Radix puerariae', CAST(60.30 AS Decimal(5, 2)), 119, CAST(1.60 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(28.00 AS Decimal(5, 2)), CAST(9.20 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (33, N'2006', N'Củ súng khô (đã bỏ vỏ)', N'Nymphaea stellata wild, dried, unshelled', CAST(8.60 AS Decimal(5, 2)), 350, CAST(16.10 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(70.30 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), CAST(2.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (34, N'2007', N'Củ từ', N'Igname Yam, Chinese', CAST(75.00 AS Decimal(5, 2)), 92, CAST(1.50 AS Decimal(5, 2)), NULL, CAST(21.50 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (35, N'2008', N'Khoai lang', N'Sweet potato, white', CAST(68.00 AS Decimal(5, 2)), 119, CAST(0.80 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(28.50 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (36, N'2009', N'Khoai lang nghệ', N'Sweet potato, yellow', CAST(69.90 AS Decimal(5, 2)), 116, CAST(1.20 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(27.10 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (37, N'2010', N'Khoai môn', N'Chinese Yam, spiny yam', CAST(70.80 AS Decimal(5, 2)), 109, CAST(1.50 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(25.20 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (38, N'2011', N'Khoai nước', N'Water-taro', CAST(72.50 AS Decimal(5, 2)), 98, CAST(1.00 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(23.30 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (39, N'2012', N'Khoai riềng', N'Edible canna', CAST(70.00 AS Decimal(5, 2)), 104, CAST(0.80 AS Decimal(5, 2)), NULL, CAST(25.10 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (40, N'2013', N'Khoai sọ', N'Taro tuber', CAST(69.00 AS Decimal(5, 2)), 114, CAST(1.80 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(26.50 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (41, N'2014', N'Khoai tây', N'Potato, white', CAST(75.00 AS Decimal(5, 2)), 93, CAST(2.00 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(20.90 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (42, N'2015', N'Miến dong', N'Vermicelli from Bermuda tuber', CAST(14.50 AS Decimal(5, 2)), 332, CAST(0.60 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(82.20 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (43, N'2016', N'Bột dong lọc', N'Bermuda flour', CAST(14.00 AS Decimal(5, 2)), 341, CAST(0.60 AS Decimal(5, 2)), NULL, CAST(84.70 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2650792' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (44, N'2017', N'Bột khoai lang', N'Sweet potato flour', CAST(14.00 AS Decimal(5, 2)), 334, CAST(2.20 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(80.20 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (45, N'2018', N'Bột khoai riềng (bột đao)', N'Canna edulis Ker, flour', CAST(14.00 AS Decimal(5, 2)), 337, CAST(0.20 AS Decimal(5, 2)), NULL, CAST(84.10 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (46, N'2019', N'Bột khoai tây (lọc)', N'Potato flour, white', CAST(13.60 AS Decimal(5, 2)), 345, CAST(1.00 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(84.40 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (47, N'2020', N'Bột sắn', N'Cassava flour', CAST(14.00 AS Decimal(5, 2)), 333, CAST(2.40 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(79.60 AS Decimal(5, 2)), CAST(2.20 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (48, N'2021', N'Bột sắn dây', N'Radix puerariae flour', CAST(14.00 AS Decimal(5, 2)), 340, CAST(0.70 AS Decimal(5, 2)), NULL, CAST(84.30 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (49, N'2022', N'Khoai lang khô', N'Sweet potato, dried', CAST(11.00 AS Decimal(5, 2)), 333, CAST(2.20 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(80.00 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), CAST(2.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (50, N'2023', N'Khoai tây khô', N'Potato, white, dried', CAST(11.00 AS Decimal(5, 2)), 330, CAST(6.60 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(75.10 AS Decimal(5, 2)), CAST(3.40 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (51, N'2024', N'Khoai tây lát chiên', N'Potato crisp, fried', CAST(3.00 AS Decimal(5, 2)), 525, CAST(2.20 AS Decimal(5, 2)), CAST(35.40 AS Decimal(5, 2)), CAST(49.30 AS Decimal(5, 2)), CAST(6.30 AS Decimal(5, 2)), CAST(3.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (52, N'2025', N'Sắn khô', N'Dried bitter cassava', CAST(11.00 AS Decimal(5, 2)), 340, CAST(3.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(80.30 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (53, N'2026', N'Trân châu sắn', N'Tapioca, pearls E.P', CAST(14.00 AS Decimal(5, 2)), 341, CAST(1.00 AS Decimal(5, 2)), NULL, CAST(84.30 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (54, N'3001', N'Cùi dưa già', N'Coconut meat, mature, raw', CAST(47.60 AS Decimal(5, 2)), 368, CAST(4.80 AS Decimal(5, 2)), CAST(36.00 AS Decimal(5, 2)), CAST(6.20 AS Decimal(5, 2)), CAST(4.20 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (55, N'3002', N'Cùi dưa non', N'Coconut meat, immature, raw', CAST(87.80 AS Decimal(5, 2)), 40, CAST(3.50 AS Decimal(5, 2)), CAST(1.70 AS Decimal(5, 2)), CAST(2.60 AS Decimal(5, 2)), CAST(3.50 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (56, N'3003', N'Đậu cô ve (hạt)', N'French bean seeds, dried', CAST(14.00 AS Decimal(5, 2)), 321, CAST(21.80 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(54.90 AS Decimal(5, 2)), CAST(3.50 AS Decimal(5, 2)), CAST(4.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (57, N'3004', N'Đậu đen (hạt)', N'Black bean seeds, dried', CAST(14.00 AS Decimal(5, 2)), 325, CAST(24.20 AS Decimal(5, 2)), CAST(1.70 AS Decimal(5, 2)), CAST(53.30 AS Decimal(5, 2)), CAST(4.00 AS Decimal(5, 2)), CAST(2.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2660849' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (58, N'3005', N'Đậu đũa (hạt)', N'Cow pea whole seeds, dried', CAST(14.00 AS Decimal(5, 2)), 320, CAST(23.70 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(51.90 AS Decimal(5, 2)), CAST(4.30 AS Decimal(5, 2)), CAST(4.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2670823' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (59, N'3006', N'Đậu Hà lan (hạt)', N'Peas garden and field, seeds, dried', CAST(13.60 AS Decimal(5, 2)), 318, CAST(22.20 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), CAST(54.10 AS Decimal(5, 2)), CAST(6.00 AS Decimal(5, 2)), CAST(2.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2670823' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (60, N'3007', N'Đậu tương (đậu nành)', N'Yellow dried soybean seeds, dried', CAST(14.00 AS Decimal(5, 2)), 400, CAST(34.00 AS Decimal(5, 2)), CAST(18.40 AS Decimal(5, 2)), CAST(24.60 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2670823' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (61, N'3008', N'Đậu trắng hạt (Đậu Tây)', N'Kidney bean whole seeds, dried', CAST(14.00 AS Decimal(5, 2)), 327, CAST(23.20 AS Decimal(5, 2)), CAST(2.10 AS Decimal(5, 2)), CAST(53.80 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), CAST(3.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2670823' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (62, N'3009', N'Đậu trứng cuốc', N'Egg bird bean seeds, dried', CAST(14.00 AS Decimal(5, 2)), 321, CAST(25.80 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(50.00 AS Decimal(5, 2)), CAST(4.80 AS Decimal(5, 2)), CAST(3.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2670823' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (63, N'3010', N'Đậu xanh (đậu tắt)', N'Mungo bean seeds, dried', CAST(14.00 AS Decimal(5, 2)), 328, CAST(23.40 AS Decimal(5, 2)), CAST(2.40 AS Decimal(5, 2)), CAST(53.10 AS Decimal(5, 2)), CAST(4.70 AS Decimal(5, 2)), CAST(2.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2670823' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (64, N'3011', N'Tên thữc phẩm (Vietnamese): 	Hạt dẻ to', N'Tªn tiÕng Anh (English): 	Chestnut, Chinese whole, raw', CAST(9.00 AS Decimal(5, 2)), 638, CAST(18.00 AS Decimal(5, 2)), CAST(59.00 AS Decimal(5, 2)), CAST(8.70 AS Decimal(5, 2)), CAST(3.50 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2670823' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (65, N'3012', N'Hạt dẻ tươi', N'Chestnut, Chinese whole, raw', CAST(44.00 AS Decimal(5, 2)), 223, CAST(4.20 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(49.10 AS Decimal(5, 2)), NULL, CAST(1.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2670823' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (66, N'3013', N'Hạt dẻ khô', N'Chestnut, dried', CAST(8.90 AS Decimal(5, 2)), 363, CAST(6.80 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(79.80 AS Decimal(5, 2)), NULL, CAST(2.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2690812' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (67, N'3014', N'Hạt đen', N'Black seed', CAST(12.00 AS Decimal(5, 2)), 472, CAST(17.30 AS Decimal(5, 2)), CAST(29.00 AS Decimal(5, 2)), CAST(35.50 AS Decimal(5, 2)), CAST(3.50 AS Decimal(5, 2)), CAST(2.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2690812' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (68, N'3015', N'Hạt điều', N'Cashew nut, common', CAST(3.40 AS Decimal(5, 2)), 605, CAST(18.40 AS Decimal(5, 2)), CAST(46.30 AS Decimal(5, 2)), CAST(28.70 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(2.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2690812' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (69, N'3016', N'Hạt mít', N'Jack fruit, seed, raw', CAST(58.20 AS Decimal(5, 2)), 166, CAST(0.70 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(38.30 AS Decimal(5, 2)), NULL, CAST(1.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2690812' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (70, N'3017', N'Lạc hạt', N'Dried peanut seed', CAST(7.50 AS Decimal(5, 2)), 573, CAST(27.50 AS Decimal(5, 2)), CAST(44.50 AS Decimal(5, 2)), CAST(15.50 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2690812' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (71, N'3018', N'Quả cọ tươi', N'Palm fruit fresh', CAST(67.10 AS Decimal(5, 2)), 178, CAST(2.30 AS Decimal(5, 2)), CAST(13.40 AS Decimal(5, 2)), CAST(12.10 AS Decimal(5, 2)), CAST(3.50 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2690812' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (72, N'3019', N'Quả đại hái tươi', N'Hodgsonia fruit', CAST(35.00 AS Decimal(5, 2)), 427, CAST(20.00 AS Decimal(5, 2)), CAST(38.00 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(3.40 AS Decimal(5, 2)), CAST(2.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2690812' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (73, N'3020', N'Vưng (đen, trắng)', N'Sesame oriental seeds, whole, dried black or white', CAST(7.60 AS Decimal(5, 2)), 568, CAST(20.10 AS Decimal(5, 2)), CAST(46.40 AS Decimal(5, 2)), CAST(17.60 AS Decimal(5, 2)), CAST(3.50 AS Decimal(5, 2)), CAST(4.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2690812' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (74, N'3021', N'Bột đậu tương đã loại béo (đậu nành)', N'Soybeans flour, defatted', CAST(14.00 AS Decimal(5, 2)), 321, CAST(49.00 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(29.00 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2690812' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (75, N'3022', N'Bột đậu tương rang chín', N'Roasted soybeans flour', CAST(10.00 AS Decimal(5, 2)), 418, CAST(41.00 AS Decimal(5, 2)), CAST(18.00 AS Decimal(5, 2)), CAST(22.90 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(5.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2690812' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (76, N'3023', N'Bột đậu xanh', N'Mungo bean flour', CAST(10.00 AS Decimal(5, 2)), 347, CAST(24.60 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(56.50 AS Decimal(5, 2)), CAST(3.90 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2690812' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (77, N'3024', N'Bột lạc', N'Peanut flour', CAST(8.00 AS Decimal(5, 2)), 575, CAST(27.50 AS Decimal(5, 2)), CAST(45.00 AS Decimal(5, 2)), CAST(15.00 AS Decimal(5, 2)), CAST(2.10 AS Decimal(5, 2)), CAST(2.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2690812' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (78, N'3025', N'Đậu phụ', N'Soybean curt cake pressed, raw', CAST(82.00 AS Decimal(5, 2)), 95, CAST(10.90 AS Decimal(5, 2)), CAST(5.40 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2700833' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (79, N'3026', N'Đậu phụ chúc', N'Curd tofu concentrated', CAST(18.50 AS Decimal(5, 2)), 414, CAST(50.20 AS Decimal(5, 2)), CAST(20.80 AS Decimal(5, 2)), CAST(6.50 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(3.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2700833' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (80, N'3027', N'Đậu phụ nướng', N'Curd tofu, fried', CAST(78.20 AS Decimal(5, 2)), 114, CAST(13.40 AS Decimal(5, 2)), CAST(6.40 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2700833' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (81, N'3028', N'Hạt bí đỏ rang', N'Pumpkin seeds, fried', CAST(3.60 AS Decimal(5, 2)), 519, CAST(35.10 AS Decimal(5, 2)), CAST(31.80 AS Decimal(5, 2)), CAST(23.00 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), CAST(4.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2700833' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (82, N'3029', N'Hạt dưa đỏ rang (dưa hấu)', N'Water melon seeds whole, fried', CAST(4.80 AS Decimal(5, 2)), 551, CAST(31.80 AS Decimal(5, 2)), CAST(39.10 AS Decimal(5, 2)), CAST(18.00 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2700833' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (83, N'3030', N'Hạt điều khô, chiên dầu', N'Cashew, common, roasted with oil', CAST(12.70 AS Decimal(5, 2)), 583, CAST(18.30 AS Decimal(5, 2)), CAST(49.30 AS Decimal(5, 2)), CAST(16.40 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(2.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2700833' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (84, N'3031', N'Sữa bột đậu nành', N'Milk flour, made from roasted soybeans', CAST(3.50 AS Decimal(5, 2)), 405, CAST(31.10 AS Decimal(5, 2)), CAST(9.70 AS Decimal(5, 2)), CAST(48.20 AS Decimal(5, 2)), CAST(2.20 AS Decimal(5, 2)), CAST(5.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2700833' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (85, N'3032', N'Sữa đậu nành (100g đậu/lít)', N'Soybean milk (100 g soybean/l)', CAST(94.40 AS Decimal(5, 2)), 28, CAST(3.10 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2700833' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (86, N'3033', N'Tào phớ', N'Tofu in light syrup (Tofu 160g, sirup 65ml)', CAST(90.00 AS Decimal(5, 2)), 37, CAST(2.30 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(6.40 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2700833' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (87, N'4001', N'Bầu', N'Calabash, Bottle gourd', CAST(95.10 AS Decimal(5, 2)), 14, CAST(0.60 AS Decimal(5, 2)), CAST(0.02 AS Decimal(5, 2)), CAST(2.90 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2700833' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (88, N'4002', N'Bí đao (bí xanh)', N'Asgourd Waxgoured, Winter melon', CAST(95.50 AS Decimal(5, 2)), 12, CAST(0.60 AS Decimal(5, 2)), NULL, CAST(2.40 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2700833' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (89, N'4003', N'Bí ngô', N'Pumpkin squash', CAST(92.00 AS Decimal(5, 2)), 27, CAST(0.30 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(6.10 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2700833' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (90, N'4004', N'Cà bát', N'Egg plant big, Brinja aubergine', CAST(92.50 AS Decimal(5, 2)), 23, CAST(1.20 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(4.00 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (91, N'4005', N'Cà chua', N'Tomato', CAST(94.00 AS Decimal(5, 2)), 20, CAST(0.60 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(4.00 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (92, N'4006', N'Cà pháo', N'Egg plant - small', CAST(92.50 AS Decimal(5, 2)), 20, CAST(1.50 AS Decimal(5, 2)), NULL, CAST(3.60 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (93, N'4007', N'Cà rốt (củ đỏ, vàng)', N'Carrots', CAST(88.50 AS Decimal(5, 2)), 39, CAST(1.50 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(7.80 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (94, N'4008', N'Cà rốt khô', N'Dried carrot', CAST(14.00 AS Decimal(5, 2)), 292, CAST(9.20 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(60.40 AS Decimal(5, 2)), CAST(9.60 AS Decimal(5, 2)), CAST(5.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (95, N'4009', N'Cà tím', N'Aubergine', CAST(92.50 AS Decimal(5, 2)), 22, CAST(1.00 AS Decimal(5, 2)), NULL, CAST(4.50 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (96, N'4010', N'Cải bắp', N'Cabbage, common', CAST(90.00 AS Decimal(5, 2)), 29, CAST(1.80 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(5.30 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (97, N'4011', N'Cải bắp đỏ', N'Cabbage, red', CAST(84.00 AS Decimal(5, 2)), 45, CAST(1.90 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(9.00 AS Decimal(5, 2)), CAST(4.00 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (98, N'4012', N'Cải bắp khô', N'Dried cabbage, white', CAST(16.00 AS Decimal(5, 2)), 245, CAST(18.00 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(39.70 AS Decimal(5, 2)), CAST(14.00 AS Decimal(5, 2)), CAST(10.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (99, N'4013', N'Cải cúc', N'Chrysanthemum. crown-daisy', CAST(93.80 AS Decimal(5, 2)), 14, CAST(1.60 AS Decimal(5, 2)), NULL, CAST(1.90 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (100, N'4014', N'Cải soong', N'Watercress', CAST(93.70 AS Decimal(5, 2)), 15, CAST(2.10 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (101, N'4015', N'Cải thìa (cải trắng)', N'Chinese cabbage, white', CAST(93.20 AS Decimal(5, 2)), 17, CAST(1.40 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(2.40 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (102, N'4016', N'Cải xanh', N'Mustard greens, India, leaves and stems', CAST(93.80 AS Decimal(5, 2)), 16, CAST(1.70 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(1.90 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (103, N'4017', N'Cần ta', N'Water drop-wort; Celery water', CAST(95.30 AS Decimal(5, 2)), 10, CAST(1.00 AS Decimal(5, 2)), NULL, CAST(1.50 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (104, N'4018', N'Cần tây', N'Celery, Chinese', CAST(85.00 AS Decimal(5, 2)), 48, CAST(3.70 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(7.90 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(1.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (105, N'4019', N'Chuối xanh', N'Banana common varieties, unripe', CAST(80.20 AS Decimal(5, 2)), 74, CAST(1.20 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(16.40 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (106, N'4020', N'Củ cải đỏ', N'Red radish oriental, raw', CAST(86.00 AS Decimal(5, 2)), 48, CAST(1.30 AS Decimal(5, 2)), NULL, CAST(10.80 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (107, N'4021', N'Củ cải trắng', N'Radish garden while, raw', CAST(92.10 AS Decimal(5, 2)), 21, CAST(1.50 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2710820' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (108, N'4022', N'Củ cải trắng khô', N'Dried radish, white', CAST(16.00 AS Decimal(5, 2)), 220, CAST(17.60 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(33.90 AS Decimal(5, 2)), CAST(17.70 AS Decimal(5, 2)), CAST(13.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2720972' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (109, N'4023', N'Củ đậu', N'Pachyrrhizus', CAST(92.00 AS Decimal(5, 2)), 28, CAST(1.00 AS Decimal(5, 2)), NULL, CAST(6.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2720972' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (110, N'4024', N'Củ niễng', N'Manchurian water-rice, Manchurian Wild rice', CAST(90.20 AS Decimal(5, 2)), 30, CAST(2.00 AS Decimal(5, 2)), NULL, CAST(5.40 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2720972' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (111, N'4025', N'Dọc củ cải (non)', N'Horse radish; dish - tree, drumstick leave', CAST(93.60 AS Decimal(5, 2)), 14, CAST(1.90 AS Decimal(5, 2)), NULL, CAST(1.60 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2720972' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (112, N'4026', N'Dọc mùng', N'Colocasia indica', CAST(96.00 AS Decimal(5, 2)), 5, CAST(0.40 AS Decimal(5, 2)), NULL, CAST(0.80 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2720972' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (113, N'4027', N'Dưa chuột', N'Cucumber', CAST(95.00 AS Decimal(5, 2)), 16, CAST(0.80 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(2.90 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2720972' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (114, N'4028', N'Dưa gang', N'Large cucumber', CAST(96.20 AS Decimal(5, 2)), 11, CAST(0.80 AS Decimal(5, 2)), NULL, CAST(2.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2720972' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (115, N'4029', N'Đậu cô ve', N'Beans, kidney, in pod, French bean; Navy been', CAST(80.00 AS Decimal(5, 2)), 73, CAST(5.00 AS Decimal(5, 2)), NULL, CAST(13.30 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2720972' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (116, N'4030', N'Đậu đũa', N'Cow-peas, yard long, Chinese long bean', CAST(83.00 AS Decimal(5, 2)), 59, CAST(6.00 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(7.90 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2720972' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (117, N'4031', N'Đậu Hà Lan', N'Green peas; field pea; Peas garden', CAST(81.00 AS Decimal(5, 2)), 72, CAST(6.50 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(10.60 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2730816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (118, N'4032', N'Đậu rồng (quả non)', N'Winged bean goabean, Indies, asparagus pea', CAST(89.50 AS Decimal(5, 2)), 34, CAST(1.90 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(6.30 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2730816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (119, N'4033', N'Đu đủ xanh', N'Papaya, unripe, raw', CAST(92.10 AS Decimal(5, 2)), 22, CAST(0.80 AS Decimal(5, 2)), NULL, CAST(4.60 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2730816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (120, N'4034', N'Gấc', N'Gac fruit, whole', CAST(77.00 AS Decimal(5, 2)), 122, CAST(2.10 AS Decimal(5, 2)), CAST(7.90 AS Decimal(5, 2)), CAST(10.50 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2730816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (121, N'4035', N'Giá đậu tương', N'Sprout of soybeans', CAST(80.80 AS Decimal(5, 2)), 79, CAST(7.70 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(8.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2730816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (122, N'4036', N'Giá đậu xanh', N'Mungobean sprouts, Green gram, Tiensin green bean', CAST(86.50 AS Decimal(5, 2)), 44, CAST(5.50 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(5.10 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2730816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (123, N'4037', N'Hành củ tươi', N'Onion, Welsh', CAST(92.50 AS Decimal(5, 2)), 26, CAST(1.30 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(4.40 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2730816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (124, N'4038', N'Hành lá (hành hoa)', N'Onion, Welsh', CAST(92.50 AS Decimal(5, 2)), 22, CAST(1.30 AS Decimal(5, 2)), NULL, CAST(4.30 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2740828' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (125, N'4039', N'Hành tây', N'Onion, common, garden', CAST(88.00 AS Decimal(5, 2)), 41, CAST(1.80 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(8.20 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2740828' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (126, N'4040', N'Hạt sen tươi', N'Lotus seed, raw', CAST(57.90 AS Decimal(5, 2)), 161, CAST(9.50 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(29.50 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2740828' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (127, N'4041', N'Hạt sen khô', N'Dried lotus seed', CAST(14.00 AS Decimal(5, 2)), 334, CAST(20.00 AS Decimal(5, 2)), CAST(2.40 AS Decimal(5, 2)), CAST(58.00 AS Decimal(5, 2)), CAST(2.20 AS Decimal(5, 2)), CAST(3.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2740828' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (128, N'4042', N'Hẹ lá', N'Onion, fragrant, Chinese leek', CAST(94.50 AS Decimal(5, 2)), 18, CAST(2.20 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2740828' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (129, N'4043', N'Hoa chuối', N'Banana, buds and flowers', CAST(92.00 AS Decimal(5, 2)), 20, CAST(1.50 AS Decimal(5, 2)), NULL, CAST(3.50 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2740828' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (130, N'4044', N'Hoa lỳ', N'Daylily, lemon flowers, Pergularia, raw', CAST(90.50 AS Decimal(5, 2)), 23, CAST(2.90 AS Decimal(5, 2)), NULL, CAST(2.80 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2740828' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (131, N'4045', N'Khế', N'Carambola; Star fruit', CAST(93.50 AS Decimal(5, 2)), 16, CAST(0.60 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(2.80 AS Decimal(5, 2)), CAST(2.60 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2740828' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (132, N'4046', N'Lá lốt', N'Lolot', CAST(86.50 AS Decimal(5, 2)), 39, CAST(4.30 AS Decimal(5, 2)), NULL, CAST(5.40 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2740828' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (133, N'4047', N'Lá me', N'Tamarind, leaves', CAST(86.00 AS Decimal(5, 2)), 46, CAST(7.50 AS Decimal(5, 2)), NULL, CAST(4.00 AS Decimal(5, 2)), NULL, CAST(2.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2740828' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (134, N'4048', N'Lá mơ lông', N'Wild plant', CAST(86.10 AS Decimal(5, 2)), 27, CAST(3.90 AS Decimal(5, 2)), NULL, CAST(2.90 AS Decimal(5, 2)), CAST(5.10 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2740828' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (135, N'4049', N'Lá sắn tươi', N'Cassava leaves', CAST(74.80 AS Decimal(5, 2)), 78, CAST(7.00 AS Decimal(5, 2)), NULL, CAST(12.60 AS Decimal(5, 2)), CAST(4.30 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2750844' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (136, N'4050', N'Măng chua', N'Bamboo shoot, unspecified', CAST(92.80 AS Decimal(5, 2)), 11, CAST(1.40 AS Decimal(5, 2)), NULL, CAST(1.40 AS Decimal(5, 2)), CAST(4.10 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2750844' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (137, N'4051', N'Măng khô', N'Dried bamboo shoots', CAST(23.00 AS Decimal(5, 2)), 157, CAST(13.00 AS Decimal(5, 2)), CAST(2.10 AS Decimal(5, 2)), CAST(21.50 AS Decimal(5, 2)), CAST(36.00 AS Decimal(5, 2)), CAST(4.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2760834' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (138, N'4052', N'Măng tây', N'Asparagus, white', CAST(93.70 AS Decimal(5, 2)), 14, CAST(2.20 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2760834' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (139, N'4053', N'Măng tre', N'Bamboo shoots, spring variety', CAST(92.00 AS Decimal(5, 2)), 15, CAST(1.70 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), CAST(4.10 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2760834' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (140, N'4054', N'Mướp', N'Gourd, sponge gourd', CAST(95.10 AS Decimal(5, 2)), 17, CAST(0.90 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(2.80 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2760834' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (141, N'4055', N'Mướp đắng', N'Balsam-pear, balsam-apple, bitter melon, bitter gourd', CAST(94.40 AS Decimal(5, 2)), 16, CAST(0.90 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(2.80 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2760834' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (142, N'4056', N'Mướp Nhật bản', N'Gourd, sponge gourd, Japanese', CAST(95.40 AS Decimal(5, 2)), 10, CAST(0.80 AS Decimal(5, 2)), NULL, CAST(1.80 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2760834' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (143, N'4057', N'Ngải cứu', N'Mugwort, common sagebrush', CAST(89.70 AS Decimal(5, 2)), 25, CAST(3.80 AS Decimal(5, 2)), NULL, CAST(2.40 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(2.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2760834' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (144, N'4058', N'Ngô bao từ', N'Corn, small variety immature, baby corn', CAST(89.20 AS Decimal(5, 2)), 40, CAST(2.20 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(7.40 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2760834' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (145, N'4059', N'Ngó sen', N'Lotus, stem underground', CAST(82.90 AS Decimal(5, 2)), 61, CAST(1.00 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(13.90 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (146, N'4060', N'Nụ mướp', N'Sponge gourd, rag, young flower', CAST(90.00 AS Decimal(5, 2)), 30, CAST(4.90 AS Decimal(5, 2)), NULL, CAST(2.50 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (147, N'4061', N'ớt đỏ to', N'Chili pepper, peppers red', CAST(92.20 AS Decimal(5, 2)), 23, CAST(1.00 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(4.00 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (148, N'4062', N'ớt vàng to', N'Peppers, yellow', CAST(91.00 AS Decimal(5, 2)), 29, CAST(1.30 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(5.50 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (149, N'4063', N'ớt xanh to', N'Peppers, green', CAST(92.00 AS Decimal(5, 2)), 25, CAST(1.30 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (150, N'4064', N'Quả dọc', NULL, CAST(95.00 AS Decimal(5, 2)), 19, CAST(0.80 AS Decimal(5, 2)), NULL, CAST(3.90 AS Decimal(5, 2)), NULL, CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (151, N'4065', N'Quả me chua', N'Tamarind fruit, pulp raw', CAST(90.30 AS Decimal(5, 2)), 27, CAST(1.90 AS Decimal(5, 2)), NULL, CAST(4.80 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (152, N'4066', N'Rau bí', N'Pumpkin leaves', CAST(93.20 AS Decimal(5, 2)), 18, CAST(2.70 AS Decimal(5, 2)), NULL, CAST(1.70 AS Decimal(5, 2)), CAST(1.70 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (153, N'4067', N'Rau câu khô', N'Dried seaweed', CAST(20.00 AS Decimal(5, 2)), 198, CAST(11.20 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(35.80 AS Decimal(5, 2)), CAST(20.80 AS Decimal(5, 2)), CAST(11.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (154, N'4068', N'Rau câu tươi', N'Seaweed fresh', CAST(86.20 AS Decimal(5, 2)), 25, CAST(1.90 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(4.10 AS Decimal(5, 2)), CAST(5.00 AS Decimal(5, 2)), CAST(2.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (155, N'4069', N'Rau diếp', N'Lettuce garden', CAST(95.70 AS Decimal(5, 2)), 14, CAST(1.20 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (156, N'4070', N'Rau đay', N'Jute potherb', CAST(91.40 AS Decimal(5, 2)), 25, CAST(2.80 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (157, N'4071', N'Rau giấp cá, diếp cá', N'Wild plant', CAST(91.50 AS Decimal(5, 2)), 22, CAST(2.90 AS Decimal(5, 2)), NULL, CAST(2.70 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (158, N'4072', N'Rau giền cơm', N'Amaranth, spineless', CAST(92.00 AS Decimal(5, 2)), 21, CAST(3.40 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (159, N'4073', N'Rau giền đỏ', N'Amaranth, sp. Red', CAST(86.20 AS Decimal(5, 2)), 41, CAST(3.30 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(6.20 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(2.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (160, N'4074', N'Rau giền trắng', N'Amaranth, sp White', CAST(86.70 AS Decimal(5, 2)), 42, CAST(3.20 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(6.30 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(1.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (161, N'4075', N'Rau húng', N'Basil sweet leaves, raw', CAST(91.40 AS Decimal(5, 2)), 20, CAST(2.20 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(3.50 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (162, N'4076', N'Rau khoai lang', N'Sweet potato, leaves', CAST(91.90 AS Decimal(5, 2)), 23, CAST(2.60 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (163, N'4077', N'Rau kinh giới', N'Sweet marjoram', CAST(90.30 AS Decimal(5, 2)), 22, CAST(2.70 AS Decimal(5, 2)), NULL, CAST(2.80 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2770816' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (164, N'4078', N'Rau má rừng', N'Wild plant', CAST(91.10 AS Decimal(5, 2)), 25, CAST(3.10 AS Decimal(5, 2)), NULL, CAST(3.10 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (165, N'4079', N'Rau má, má mơ', N'Wort, India penny', CAST(88.20 AS Decimal(5, 2)), 20, CAST(3.20 AS Decimal(5, 2)), NULL, CAST(1.80 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (166, N'4080', N'Rau mồng tơi', N'Malabar night shade, Vinespinach, Ceylon spinach', CAST(93.20 AS Decimal(5, 2)), 14, CAST(2.00 AS Decimal(5, 2)), NULL, CAST(1.40 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (167, N'4081', N'Rau mùi', N'Coriander', CAST(93.30 AS Decimal(5, 2)), 16, CAST(2.60 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (168, N'4082', N'Rau mùi tàu', N'Parsley, curley', CAST(92.00 AS Decimal(5, 2)), 25, CAST(2.10 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(2.40 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (169, N'4083', N'Rau muống', N'Swamp cabbage, water spinach, water convol', CAST(92.00 AS Decimal(5, 2)), 25, CAST(3.20 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(2.10 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (170, N'4084', N'Rau muống khô', N'Water spinach, dried', CAST(14.00 AS Decimal(5, 2)), 245, CAST(34.20 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(23.00 AS Decimal(5, 2)), CAST(12.00 AS Decimal(5, 2)), CAST(15.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (171, N'4085', N'Rau ngổ', N'Limnophila aromatic', CAST(93.30 AS Decimal(5, 2)), 15, CAST(1.50 AS Decimal(5, 2)), NULL, CAST(2.30 AS Decimal(5, 2)), CAST(2.10 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (172, N'4086', N'Rau ngót', N'Sauropus, sp. leaves', CAST(86.40 AS Decimal(5, 2)), 35, CAST(5.30 AS Decimal(5, 2)), NULL, CAST(3.40 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(2.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (173, N'4087', N'Rau ngót khô', N'Sauropus, dried', CAST(14.00 AS Decimal(5, 2)), 239, CAST(32.20 AS Decimal(5, 2)), CAST(2.60 AS Decimal(5, 2)), CAST(21.80 AS Decimal(5, 2)), CAST(15.00 AS Decimal(5, 2)), CAST(14.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (174, N'4088', N'Rau răm', N'Polygonum odoratum', CAST(86.70 AS Decimal(5, 2)), 30, CAST(4.70 AS Decimal(5, 2)), NULL, CAST(2.80 AS Decimal(5, 2)), CAST(3.80 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (175, N'4089', N'Rau rút', N'Neptunia, dismanthus', CAST(90.40 AS Decimal(5, 2)), 28, CAST(5.10 AS Decimal(5, 2)), NULL, CAST(1.80 AS Decimal(5, 2)), CAST(1.90 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (176, N'4090', N'Rau sà lách', N'Lettuce, garden asparagus', CAST(95.00 AS Decimal(5, 2)), 17, CAST(1.50 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (177, N'4091', N'Rau sam', N'Purslane, common', CAST(93.60 AS Decimal(5, 2)), 18, CAST(1.40 AS Decimal(5, 2)), NULL, CAST(3.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (178, N'4092', N'Rau sắng (chùa Hương)', N'Perfume pagoda wild plant', CAST(82.40 AS Decimal(5, 2)), 48, CAST(6.50 AS Decimal(5, 2)), NULL, CAST(5.50 AS Decimal(5, 2)), CAST(3.40 AS Decimal(5, 2)), CAST(2.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (179, N'4093', N'Rau tàu bay', N'Gynura crepidioides', CAST(93.10 AS Decimal(5, 2)), 18, CAST(2.50 AS Decimal(5, 2)), NULL, CAST(1.90 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (180, N'4094', N'Rau thơm', N'Mint leaves', CAST(91.70 AS Decimal(5, 2)), 18, CAST(2.00 AS Decimal(5, 2)), NULL, CAST(2.40 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (181, N'4095', N'Sấu xanh', N'Dracontomelum fruit, unripe, raw', CAST(94.70 AS Decimal(5, 2)), 19, CAST(1.80 AS Decimal(5, 2)), NULL, CAST(3.00 AS Decimal(5, 2)), NULL, CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2785762' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (182, N'4096', N'Su hào', N'Kohlrabi', CAST(88.00 AS Decimal(5, 2)), 37, CAST(2.80 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(6.20 AS Decimal(5, 2)), CAST(1.70 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2795884' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (183, N'4097', N'Su hào khô', N'Dried kohlrabi', CAST(15.00 AS Decimal(5, 2)), 261, CAST(20.00 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), CAST(42.10 AS Decimal(5, 2)), CAST(12.50 AS Decimal(5, 2)), CAST(9.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2795884' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (184, N'4098', N'Su su, quả', N'Chayote, fruit raw', CAST(94.00 AS Decimal(5, 2)), 19, CAST(0.80 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2795884' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (185, N'4099', N'Súp lơ trắng', N'Cauliflower, white', CAST(90.90 AS Decimal(5, 2)), 30, CAST(2.50 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(4.80 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2795884' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (186, N'4100', N'Súp lơ xanh', N'Cauliflower, green', CAST(89.80 AS Decimal(5, 2)), 26, CAST(3.00 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(2.90 AS Decimal(5, 2)), CAST(3.20 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2795884' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (187, N'4101', N'Thìa là', N'Dill', CAST(88.40 AS Decimal(5, 2)), 28, CAST(2.60 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(5.00 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2795884' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (188, N'4102', N'Tía tô', N'Balm-mint, garden- balm', CAST(89.10 AS Decimal(5, 2)), 25, CAST(2.90 AS Decimal(5, 2)), NULL, CAST(3.40 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (189, N'4103', N'Tỏi ta', N'Garlic bulbs', CAST(67.70 AS Decimal(5, 2)), 121, CAST(6.00 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(23.00 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (190, N'4104', N'Tỏi tây (cả lá)', N'Chinese Leek, Onion fragrant', CAST(90.00 AS Decimal(5, 2)), 29, CAST(1.40 AS Decimal(5, 2)), NULL, CAST(5.90 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (191, N'4105', N'Trám đen chín', N'Variety of canarium', CAST(77.00 AS Decimal(5, 2)), 114, CAST(2.50 AS Decimal(5, 2)), CAST(10.00 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), CAST(4.90 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (192, N'4106', N'Trám xanh sống, trám trắng', N'Chinese olive', CAST(86.70 AS Decimal(5, 2)), 37, CAST(1.20 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(4.40 AS Decimal(5, 2)), CAST(4.90 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (193, N'4107', N'Xương sông', N'Edible herbaceous plant', CAST(91.70 AS Decimal(5, 2)), 15, CAST(2.20 AS Decimal(5, 2)), NULL, CAST(1.50 AS Decimal(5, 2)), CAST(3.20 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (194, N'4108', N'Cà chua muối', N'Tomato, pickled', CAST(94.00 AS Decimal(5, 2)), 6, CAST(0.60 AS Decimal(5, 2)), NULL, CAST(1.00 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (195, N'4109', N'Cà muối nén', N'Eggplant, garden, brinjan; aubergine, pickled', CAST(81.00 AS Decimal(5, 2)), 13, CAST(1.30 AS Decimal(5, 2)), NULL, CAST(2.00 AS Decimal(5, 2)), CAST(1.70 AS Decimal(5, 2)), CAST(14.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (196, N'4110', N'Cà muối sổi', N'Eggplant, garden, brinjan; aubergine, pickled', CAST(84.30 AS Decimal(5, 2)), 16, CAST(1.50 AS Decimal(5, 2)), NULL, CAST(2.50 AS Decimal(5, 2)), CAST(1.70 AS Decimal(5, 2)), CAST(10.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (197, N'4111', N'Dưa cải bắp', N'Cabbage Chinese, pickled', CAST(90.90 AS Decimal(5, 2)), 18, CAST(1.20 AS Decimal(5, 2)), NULL, CAST(3.30 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (198, N'4112', N'Dưa cải bẹ', N'Mustard green, pickled', CAST(90.10 AS Decimal(5, 2)), 17, CAST(1.80 AS Decimal(5, 2)), NULL, CAST(2.40 AS Decimal(5, 2)), CAST(2.10 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (199, N'4113', N'Dưa cải sen', N'Rape bird, pickled', CAST(93.80 AS Decimal(5, 2)), 10, CAST(1.10 AS Decimal(5, 2)), NULL, CAST(1.30 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (200, N'4114', N'Dưa chuột muối', N'Cucumber, pickled', CAST(92.10 AS Decimal(5, 2)), 13, CAST(0.80 AS Decimal(5, 2)), NULL, CAST(2.50 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(3.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (201, N'4115', N'Dưa giá (đậu xanh)', N'Mung bean sprouts, pickled', CAST(86.20 AS Decimal(5, 2)), 39, CAST(5.00 AS Decimal(5, 2)), NULL, CAST(4.80 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (202, N'4116', N'Hành củ muối', N'Onion, pickled', CAST(88.50 AS Decimal(5, 2)), 22, CAST(1.40 AS Decimal(5, 2)), NULL, CAST(4.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(5.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2805866' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (203, N'4117', N'Kiệu muối', N'Onion shallot, scallion, pickled', CAST(89.50 AS Decimal(5, 2)), 24, CAST(1.30 AS Decimal(5, 2)), NULL, CAST(4.70 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(3.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (204, N'4118', N'Nhút (muối từ mít non, ngọn đậu xanh non...)', N'Mix pickled from young jack fruit', CAST(84.90 AS Decimal(5, 2)), 16, CAST(2.50 AS Decimal(5, 2)), NULL, CAST(1.50 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), CAST(8.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (205, N'4119', N'Men bia khô', N'Brewer''s yeast, fried', CAST(7.50 AS Decimal(5, 2)), 340, CAST(52.50 AS Decimal(5, 2)), CAST(3.20 AS Decimal(5, 2)), CAST(25.40 AS Decimal(5, 2)), CAST(6.60 AS Decimal(5, 2)), CAST(4.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (206, N'4120', N'Men bia tươi', N'Baker''s yeast, fresh', CAST(74.60 AS Decimal(5, 2)), 95, CAST(16.20 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (207, N'4121', N'Mộc nhĩ', N'Jew''s ear, Juda''s ear, dried, Wood-ear, Tender variety', CAST(11.40 AS Decimal(5, 2)), 304, CAST(10.60 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(65.00 AS Decimal(5, 2)), CAST(7.00 AS Decimal(5, 2)), CAST(5.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (208, N'4122', N'Nấm hương khô', N'Mushroom Chinese, dried', CAST(13.00 AS Decimal(5, 2)), 274, CAST(36.00 AS Decimal(5, 2)), CAST(4.00 AS Decimal(5, 2)), CAST(23.50 AS Decimal(5, 2)), CAST(17.00 AS Decimal(5, 2)), CAST(6.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (209, N'4123', N'Nấm hương tươi', N'Mushroom, Chinese, raw', CAST(87.00 AS Decimal(5, 2)), 39, CAST(5.50 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(3.10 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (210, N'4124', N'Nấm mỡ (Nấm tây)', N'Mushroom', CAST(90.40 AS Decimal(5, 2)), 32, CAST(4.00 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(3.40 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (211, N'4125', N'Nấm rơm', N'Mushroom, straw', CAST(87.90 AS Decimal(5, 2)), 57, CAST(3.60 AS Decimal(5, 2)), CAST(3.20 AS Decimal(5, 2)), CAST(3.40 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (212, N'4126', N'Nấm thường tươi', N'Mushroom, common', CAST(88.00 AS Decimal(5, 2)), 34, CAST(4.60 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(2.20 AS Decimal(5, 2)), CAST(3.50 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (213, N'5001', N'Bưởi', N'Pomelo, Pummelo; Shaddock', CAST(91.40 AS Decimal(5, 2)), 30, CAST(0.20 AS Decimal(5, 2)), NULL, CAST(7.30 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (214, N'5002', N'Cam', N'Orange', CAST(88.80 AS Decimal(5, 2)), 38, CAST(0.90 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(8.30 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (215, N'5003', N'Chanh', N'Lemon', CAST(92.50 AS Decimal(5, 2)), 24, CAST(0.90 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (216, N'5004', N'Chôm chôm', N'Rambutan', CAST(80.30 AS Decimal(5, 2)), 72, CAST(1.50 AS Decimal(5, 2)), NULL, CAST(16.40 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (217, N'5005', N'Chuối khô', N'Banana, dried', CAST(22.00 AS Decimal(5, 2)), 292, CAST(5.00 AS Decimal(5, 2)), NULL, CAST(68.00 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), CAST(2.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (218, N'5006', N'Chuối tây', N'Banana', CAST(83.20 AS Decimal(5, 2)), 56, CAST(0.90 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(12.40 AS Decimal(5, 2)), CAST(2.60 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (219, N'5007', N'Chuối tiêu', N'Banana, dwarf', CAST(74.40 AS Decimal(5, 2)), 97, CAST(1.50 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(22.20 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (220, N'5008', N'Dâu gia', N'Blackberry', CAST(92.80 AS Decimal(5, 2)), 27, CAST(0.60 AS Decimal(5, 2)), NULL, CAST(6.20 AS Decimal(5, 2)), NULL, CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (221, N'5009', N'Dâu tây', N'Strawberry', CAST(84.90 AS Decimal(5, 2)), 43, CAST(1.80 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(8.10 AS Decimal(5, 2)), CAST(4.00 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2815860' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (222, N'5010', N'Dưa bở', N'Musk melon, Spanish melon, Cantaloupe', CAST(94.50 AS Decimal(5, 2)), 18, CAST(0.50 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (223, N'5011', N'Dưa hấu', N'Watermelon', CAST(95.50 AS Decimal(5, 2)), 16, CAST(1.20 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (224, N'5012', N'Dưa hồng', N'Honey dew melon', CAST(95.00 AS Decimal(5, 2)), 17, CAST(0.30 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(3.70 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (225, N'5013', N'Dưa lê', NULL, CAST(93.30 AS Decimal(5, 2)), 18, CAST(0.40 AS Decimal(5, 2)), NULL, CAST(4.20 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (226, N'5014', N'Dứa ta', N'Pineapple, wild', CAST(91.50 AS Decimal(5, 2)), 29, CAST(0.80 AS Decimal(5, 2)), NULL, CAST(6.50 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (227, N'5015', N'Dứa tây', N'Pineapple', CAST(89.70 AS Decimal(5, 2)), 38, CAST(0.50 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(8.80 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (228, N'5016', N'Đào', N'Peach', CAST(90.50 AS Decimal(5, 2)), 31, CAST(0.90 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(6.30 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (229, N'5017', N'Đu đủ chín', N'Papaya, ripe', CAST(90.10 AS Decimal(5, 2)), 36, CAST(1.00 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(7.60 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (230, N'5018', N'Gioi', N'Ohia; Malaya roseapple', CAST(93.00 AS Decimal(5, 2)), 16, CAST(0.40 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(3.20 AS Decimal(5, 2)), CAST(2.90 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (231, N'5019', N'Hồng bì', N'Wampee, Chinese; Wampi', CAST(88.20 AS Decimal(5, 2)), 34, CAST(1.40 AS Decimal(5, 2)), NULL, CAST(7.20 AS Decimal(5, 2)), CAST(2.40 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (232, N'5020', N'Hồng đỏ', N'Persimmon kaki, soft type, ripe', CAST(90.00 AS Decimal(5, 2)), 29, CAST(0.70 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(6.00 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (233, N'5021', N'Hồng ngâm', N'Persimmon kaki, Hard-type, ripe', CAST(87.50 AS Decimal(5, 2)), 38, CAST(0.90 AS Decimal(5, 2)), NULL, CAST(8.60 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (234, N'5022', N'Hồng xiêm', N'Sapodilla, sapota ponderosa', CAST(85.70 AS Decimal(5, 2)), 48, CAST(0.50 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(10.00 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (235, N'5023', N'Lê', N'Pear', CAST(87.80 AS Decimal(5, 2)), 45, CAST(0.70 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(10.20 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (236, N'5024', N'Lữu', N'Pome granate', CAST(79.60 AS Decimal(5, 2)), 70, CAST(0.60 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(16.20 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (237, N'5025', N'Mãng cầu xiêm', N'Siamese custard apple, soursop', CAST(84.70 AS Decimal(5, 2)), 53, CAST(1.80 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(10.10 AS Decimal(5, 2)), CAST(1.90 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (238, N'5026', N'Mắc coọc', N'Pyrus pashia Ham', CAST(93.80 AS Decimal(5, 2)), 24, CAST(0.20 AS Decimal(5, 2)), NULL, CAST(5.70 AS Decimal(5, 2)), NULL, CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (239, N'5027', N'Mận', N'Japanese, plum', CAST(94.10 AS Decimal(5, 2)), 20, CAST(0.60 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(3.90 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2825895' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (240, N'5028', N'Mít dai', N'Jackfruit, jackfruit mature', CAST(85.40 AS Decimal(5, 2)), 50, CAST(0.60 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(11.10 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2840515' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (241, N'5029', N'Mít khô', N'Dried jackfruit', CAST(26.00 AS Decimal(5, 2)), 280, CAST(2.90 AS Decimal(5, 2)), NULL, CAST(67.00 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), CAST(2.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2840515' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (242, N'5030', N'Mít mật', N'Jack fruit, honey', CAST(82.20 AS Decimal(5, 2)), 62, CAST(1.50 AS Decimal(5, 2)), NULL, CAST(14.00 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2840515' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (243, N'5031', N'Mơ', N'Apricot, Apricot nectar', CAST(87.10 AS Decimal(5, 2)), 48, CAST(0.90 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(10.10 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2840515' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (244, N'5032', N'Mơ khô', N'Apricot dried, unsulfured', CAST(25.90 AS Decimal(5, 2)), 273, CAST(3.00 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(62.80 AS Decimal(5, 2)), CAST(4.10 AS Decimal(5, 2)), CAST(3.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2840515' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (245, N'5033', N'Muỗm, quéo', N'Mango, common; Indian mango, unripe', CAST(82.90 AS Decimal(5, 2)), 67, CAST(0.60 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(15.30 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2840515' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (246, N'5034', N'Na', N'Sugarapple, sweetsop', CAST(82.50 AS Decimal(5, 2)), 66, CAST(1.60 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(14.20 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2840515' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (247, N'5035', N'Nhãn', N'Longan', CAST(86.30 AS Decimal(5, 2)), 48, CAST(0.90 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(10.90 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2840515' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (248, N'5036', N'Nhãn khô', N'Longan, dried', CAST(25.00 AS Decimal(5, 2)), 285, CAST(4.30 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(65.90 AS Decimal(5, 2)), CAST(1.70 AS Decimal(5, 2)), CAST(2.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2840515' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (249, N'5037', N'Nho ngọt', N'Grape, European, sweet', CAST(82.10 AS Decimal(5, 2)), 68, CAST(0.40 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(16.30 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2840515' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (250, N'5038', N'Nho ta (nho chua)', N'Grape fruit, sour', CAST(93.60 AS Decimal(5, 2)), 14, CAST(0.40 AS Decimal(5, 2)), NULL, CAST(3.10 AS Decimal(5, 2)), CAST(2.40 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2840515' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (251, N'5039', N'Nhót', N'Silver berry', CAST(94.00 AS Decimal(5, 2)), 13, CAST(1.20 AS Decimal(5, 2)), NULL, CAST(2.10 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2850650' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (252, N'5040', N'ổi', N'Guava, common', CAST(85.00 AS Decimal(5, 2)), 38, CAST(0.60 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(6.80 AS Decimal(5, 2)), CAST(6.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2850650' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (253, N'5041', N'Quả bơ vỏ tím', N'Avocado, purple', CAST(88.30 AS Decimal(5, 2)), 74, CAST(1.80 AS Decimal(5, 2)), CAST(6.20 AS Decimal(5, 2)), CAST(2.80 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2850650' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (254, N'5042', N'Quả bơ vỏ xanh', N'Avocado, green', CAST(85.40 AS Decimal(5, 2)), 101, CAST(1.90 AS Decimal(5, 2)), CAST(9.40 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2850650' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (255, N'5043', N'Quả cóc', N'-', CAST(83.90 AS Decimal(5, 2)), 58, CAST(1.80 AS Decimal(5, 2)), NULL, CAST(12.80 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2850650' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (256, N'5044', N'Quả thanh long', N'Dragon''s eyes fruit', CAST(87.60 AS Decimal(5, 2)), 40, CAST(1.30 AS Decimal(5, 2)), NULL, CAST(8.70 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2850650' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (257, N'5045', N'Quả trứng gà', N'-', CAST(73.40 AS Decimal(5, 2)), 106, CAST(4.30 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(21.30 AS Decimal(5, 2)), NULL, CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2850650' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (258, N'5046', N'Quất chín (cả vỏ)', N'Mandarin, whole fruit', CAST(89.00 AS Decimal(5, 2)), 26, CAST(0.90 AS Decimal(5, 2)), NULL, CAST(5.50 AS Decimal(5, 2)), CAST(4.10 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2865579' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (259, N'5047', N'Quít', N'Tangerine; Orange; Mandarin', CAST(89.50 AS Decimal(5, 2)), 39, CAST(0.80 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(8.30 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2865579' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (260, N'5048', N'Sầu riêng', N'Durian, Civet', CAST(66.80 AS Decimal(5, 2)), 132, CAST(2.50 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(26.90 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2865579' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (261, N'5049', N'Sấu chín', N'Sanpidus, ripe', CAST(87.00 AS Decimal(5, 2)), 38, CAST(1.30 AS Decimal(5, 2)), NULL, CAST(8.20 AS Decimal(5, 2)), CAST(2.70 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2865579' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (262, N'5050', N'Táo ta', N'Jujube, common or Chinese; Chinese date', CAST(89.50 AS Decimal(5, 2)), 38, CAST(0.80 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(8.30 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2865579' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (263, N'5051', N'Táo tây', N'Apple, common, domestic', CAST(87.20 AS Decimal(5, 2)), 48, CAST(0.50 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(11.00 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2865579' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (264, N'5052', N'Vải', N'Litchi; lychee', CAST(87.80 AS Decimal(5, 2)), 45, CAST(0.70 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(9.60 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2865579' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (265, N'5053', N'Vải khô', N'Litchi, dried with shells', CAST(34.90 AS Decimal(5, 2)), 260, CAST(3.00 AS Decimal(5, 2)), CAST(1.90 AS Decimal(5, 2)), CAST(57.60 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2865579' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (266, N'5054', N'Vú sữa', N'Starapple, cainito', CAST(86.50 AS Decimal(5, 2)), 42, CAST(1.00 AS Decimal(5, 2)), NULL, CAST(9.40 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (267, N'5055', N'Xoài chín', N'Mango, common; India mango, ripe', CAST(82.60 AS Decimal(5, 2)), 62, CAST(0.60 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(14.10 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (268, N'5056', N'Quả kiwi', N'Kiwi fruit', CAST(83.10 AS Decimal(5, 2)), 56, CAST(1.10 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(11.70 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (269, N'6001', N'Bơ', N'Butter, unsalted', CAST(15.40 AS Decimal(5, 2)), 756, CAST(0.50 AS Decimal(5, 2)), CAST(83.50 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), NULL, CAST(0.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (270, N'6002', N'Dầu thảo mộc (Lạc, vưng, cám...)', N'Vegetable oil, mix', CAST(0.30 AS Decimal(5, 2)), 897, NULL, CAST(99.70 AS Decimal(5, 2)), NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (271, N'6003', N'Mỡ lợn muối', N'Lard, salted', CAST(3.00 AS Decimal(5, 2)), 827, CAST(2.00 AS Decimal(5, 2)), CAST(91.00 AS Decimal(5, 2)), NULL, NULL, CAST(4.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (272, N'6004', N'Mỡ lợn nước', N'Lard, liquid', CAST(0.40 AS Decimal(5, 2)), 896, NULL, CAST(99.60 AS Decimal(5, 2)), NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (273, N'6005', N'Bơ thữc vật', N'Butter-margarine blend, stick, unsalted', CAST(18.50 AS Decimal(5, 2)), 729, CAST(0.50 AS Decimal(5, 2)), CAST(80.70 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), NULL, CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (274, N'6006', N'Dầu bông', N'Cottonseed oil, salad or cooking', NULL, 900, NULL, CAST(100.00 AS Decimal(5, 2)), NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (275, N'6007', N'Dầu cám gạo', N'Rice bran oil', NULL, 900, NULL, CAST(100.00 AS Decimal(5, 2)), NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (276, N'6008', N'Dầu cọ', N'Palm oil', NULL, 900, NULL, CAST(100.00 AS Decimal(5, 2)), NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (277, N'6009', N'Dầu dưa', N'Coconut oil', NULL, 900, NULL, CAST(100.00 AS Decimal(5, 2)), NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (278, N'6010', N'Dầu đậu tương', N'Soybean oil, salad or cooking', NULL, 900, NULL, CAST(100.00 AS Decimal(5, 2)), NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (279, N'6011', N'Dầu lạc', N'Peanut oil, salad or cooking', NULL, 900, NULL, CAST(100.00 AS Decimal(5, 2)), NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (280, N'6012', N'Dầu mè', N'Sesame oil, salad or cooking', NULL, 900, NULL, CAST(100.00 AS Decimal(5, 2)), NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (281, N'6013', N'Dầu ngô', N'Corn oil, salad or cooking', NULL, 900, NULL, CAST(100.00 AS Decimal(5, 2)), NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.2875676' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (282, N'6014', N'Dầu oliu', N'Olive oil, salad or cooking', NULL, 900, NULL, CAST(100.00 AS Decimal(5, 2)), NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.2885663' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (283, N'7001', N'Thịt bê mỡ', N'Veal meat, lean and fat', CAST(72.80 AS Decimal(5, 2)), 144, CAST(19.00 AS Decimal(5, 2)), CAST(7.50 AS Decimal(5, 2)), NULL, NULL, CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2885663' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (284, N'7002', N'Thịt bê nạc', N'Veal meat, lean only', CAST(78.20 AS Decimal(5, 2)), 85, CAST(20.00 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), NULL, NULL, CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2885663' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (285, N'7003', N'Tên thữc phẩm (Vietnamese): 	Thịt bò loại I', N'Tªn tiÕng Anh (English): 	Beef, grade I', CAST(74.10 AS Decimal(5, 2)), 118, CAST(21.00 AS Decimal(5, 2)), CAST(3.80 AS Decimal(5, 2)), NULL, NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2885663' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (286, N'7004', N'Tên thữc phẩm (Vietnamese): 	Thịt bò loại II', N'Tªn tiÕng Anh (English): 	Beef, grade II', CAST(70.50 AS Decimal(5, 2)), 167, CAST(18.00 AS Decimal(5, 2)), CAST(10.50 AS Decimal(5, 2)), NULL, NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2885663' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (287, N'7005', N'Thịt bò, lưng, nạc', N'Beef, top loin, seperable lean only, trimmed to 1/8" fat, prime, raw', CAST(72.70 AS Decimal(5, 2)), 127, CAST(23.10 AS Decimal(5, 2)), CAST(3.90 AS Decimal(5, 2)), NULL, NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2885663' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (288, N'7006', N'Thịt bò, lưng, nạc và mỡ', N'Beef, top loin, seperable lean only, trimmed to 1/4" fat, prime, raw', CAST(67.40 AS Decimal(5, 2)), 182, CAST(21.50 AS Decimal(5, 2)), CAST(10.70 AS Decimal(5, 2)), NULL, NULL, CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2885663' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (289, N'7007', N'Thịt bồ câu ra ràng', N'Pigeon young bird flesh skin and giblets', CAST(51.70 AS Decimal(5, 2)), 340, CAST(17.50 AS Decimal(5, 2)), CAST(30.00 AS Decimal(5, 2)), NULL, NULL, CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2885663' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (290, N'7008', N'Thịt chó sấn', N'Dog meat', CAST(53.00 AS Decimal(5, 2)), 338, CAST(16.00 AS Decimal(5, 2)), CAST(30.40 AS Decimal(5, 2)), NULL, NULL, CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2885663' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (291, N'7009', N'Thịt chó vai', N'Dog, shoulder', CAST(63.80 AS Decimal(5, 2)), 230, CAST(18.00 AS Decimal(5, 2)), CAST(17.60 AS Decimal(5, 2)), NULL, NULL, CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2885663' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (292, N'7010', N'Thịt cưu, nạc', N'Mutton meat, lean', CAST(65.80 AS Decimal(5, 2)), 219, CAST(16.40 AS Decimal(5, 2)), CAST(17.00 AS Decimal(5, 2)), NULL, NULL, CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2885663' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (293, N'7011', N'Thịt dê, nạc', N'Goat, meat, lean', CAST(74.40 AS Decimal(5, 2)), 122, CAST(20.70 AS Decimal(5, 2)), CAST(4.30 AS Decimal(5, 2)), NULL, NULL, CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (294, N'7012', N'Thịt gà rừng', N'Grouse field chicken', CAST(69.70 AS Decimal(5, 2)), 141, CAST(24.40 AS Decimal(5, 2)), CAST(4.80 AS Decimal(5, 2)), NULL, NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (295, N'7013', N'Thịt gà ta', N'Chicken meat, average', CAST(65.60 AS Decimal(5, 2)), 199, CAST(20.30 AS Decimal(5, 2)), CAST(13.10 AS Decimal(5, 2)), NULL, NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (296, N'7014', N'Thịt gà tây', N'Turkey raw flesh and skin giblets', CAST(63.60 AS Decimal(5, 2)), 218, CAST(20.10 AS Decimal(5, 2)), CAST(15.30 AS Decimal(5, 2)), NULL, NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (297, N'7015', N'Thịt hươu', N'Deer meat', CAST(77.90 AS Decimal(5, 2)), 94, CAST(19.00 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), NULL, NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (298, N'7016', N'Thịt lợn mỡ', N'Pork, lean and fat', CAST(47.50 AS Decimal(5, 2)), 394, CAST(14.50 AS Decimal(5, 2)), CAST(37.30 AS Decimal(5, 2)), NULL, NULL, CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (299, N'7017', N'Thịt lợn nạc', N'Pork, lean', CAST(73.00 AS Decimal(5, 2)), 139, CAST(19.00 AS Decimal(5, 2)), CAST(7.00 AS Decimal(5, 2)), NULL, NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (300, N'7018', N'Thịt lợn nừa nạc, nừa mỡ', N'Pork, medium fat', CAST(60.90 AS Decimal(5, 2)), 260, CAST(16.50 AS Decimal(5, 2)), CAST(21.50 AS Decimal(5, 2)), NULL, NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (301, N'7019', N'Thịt ngỗng', N'Goose', CAST(46.10 AS Decimal(5, 2)), 409, CAST(14.00 AS Decimal(5, 2)), CAST(39.20 AS Decimal(5, 2)), NULL, NULL, CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (302, N'7020', N'Thịt ngữa', N'Horse meat', CAST(66.80 AS Decimal(5, 2)), 176, CAST(21.50 AS Decimal(5, 2)), CAST(10.00 AS Decimal(5, 2)), NULL, NULL, CAST(1.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (303, N'7021', N'Thịt thỏ nhà', N'Rabbit meat, raw', CAST(69.30 AS Decimal(5, 2)), 158, CAST(21.50 AS Decimal(5, 2)), CAST(8.00 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (304, N'7022', N'Thịt thỏ rừng', N'Hare rabbit (field or wild)', CAST(74.30 AS Decimal(5, 2)), 103, CAST(23.50 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (305, N'7023', N'Thịt trâu', N'Buffalo meat, average', CAST(76.30 AS Decimal(5, 2)), 97, CAST(20.40 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2895668' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (306, N'7024', N'Thịt trâu bắp', N'Buffalo meat (shoulder)', CAST(74.20 AS Decimal(5, 2)), 115, CAST(21.90 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), NULL, NULL, CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (307, N'7025', N'Thịt trâu cổ', N'Buffalo meat, neck', CAST(74.90 AS Decimal(5, 2)), 112, CAST(20.90 AS Decimal(5, 2)), CAST(3.10 AS Decimal(5, 2)), NULL, NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (308, N'7026', N'Thịt trâu đùi', N'Buffalo meat, leg', CAST(74.90 AS Decimal(5, 2)), 112, CAST(21.20 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), NULL, NULL, CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (309, N'7027', N'Thịt trâu thăn', N'Buffalo meat, lean (loin)', CAST(73.00 AS Decimal(5, 2)), 121, CAST(22.80 AS Decimal(5, 2)), CAST(3.30 AS Decimal(5, 2)), NULL, NULL, CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (310, N'7028', N'Thịt vịt', N'Duck meat, average', CAST(59.50 AS Decimal(5, 2)), 267, CAST(17.80 AS Decimal(5, 2)), CAST(21.80 AS Decimal(5, 2)), NULL, NULL, CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (311, N'7029', N'Bầu dục bò', N'Beef, kidney', CAST(84.30 AS Decimal(5, 2)), 67, CAST(12.50 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (312, N'7030', N'Bầu dục lợn', N'Pork, kidney', CAST(82.50 AS Decimal(5, 2)), 81, CAST(13.00 AS Decimal(5, 2)), CAST(3.10 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (313, N'7031', N'Bì lợn', N'Pork skin', CAST(73.30 AS Decimal(5, 2)), 118, CAST(23.30 AS Decimal(5, 2)), CAST(2.70 AS Decimal(5, 2)), NULL, NULL, CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (314, N'7032', N'Chân giò lợn', N'Pork, leg', CAST(64.70 AS Decimal(5, 2)), 230, CAST(15.70 AS Decimal(5, 2)), CAST(18.60 AS Decimal(5, 2)), NULL, NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (315, N'7033', N'Dạ dày bò', N'Stomach, beef', CAST(80.50 AS Decimal(5, 2)), 97, CAST(14.80 AS Decimal(5, 2)), CAST(4.20 AS Decimal(5, 2)), NULL, NULL, CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (316, N'7034', N'Dạ dày lợn', N'Stomach, hog', CAST(81.70 AS Decimal(5, 2)), 85, CAST(14.60 AS Decimal(5, 2)), CAST(2.90 AS Decimal(5, 2)), NULL, NULL, CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (317, N'7035', N'Đầu bò', N'Head beef (without tongue, brain, ears).', CAST(68.70 AS Decimal(5, 2)), 185, CAST(18.10 AS Decimal(5, 2)), CAST(12.50 AS Decimal(5, 2)), NULL, NULL, CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (318, N'7036', N'Đầu lợn', N'Hog, head', CAST(54.60 AS Decimal(5, 2)), 335, CAST(13.40 AS Decimal(5, 2)), CAST(31.30 AS Decimal(5, 2)), NULL, NULL, CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (319, N'7037', N'Đuôi bò', N'Beef, tail', CAST(73.00 AS Decimal(5, 2)), 137, CAST(19.70 AS Decimal(5, 2)), CAST(6.50 AS Decimal(5, 2)), NULL, NULL, CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (320, N'7038', N'Đuôi lợn', N'Hog, tail', CAST(41.80 AS Decimal(5, 2)), 467, CAST(10.80 AS Decimal(5, 2)), CAST(47.10 AS Decimal(5, 2)), NULL, NULL, CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2905726' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (321, N'7039', N'Gan bò', N'Beef, liver', CAST(75.20 AS Decimal(5, 2)), 109, CAST(17.40 AS Decimal(5, 2)), CAST(3.10 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), NULL, CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2915713' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (322, N'7040', N'Gan gà', N'Chicken liver', CAST(75.00 AS Decimal(5, 2)), 111, CAST(18.20 AS Decimal(5, 2)), CAST(3.40 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), NULL, CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2915713' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (323, N'7041', N'Gan lợn', N'Pork liver', CAST(74.10 AS Decimal(5, 2)), 116, CAST(18.80 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), NULL, CAST(1.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2915713' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (324, N'7042', N'Gan vịt', N'Duck liver', CAST(74.10 AS Decimal(5, 2)), 122, CAST(17.10 AS Decimal(5, 2)), CAST(4.70 AS Decimal(5, 2)), CAST(2.80 AS Decimal(5, 2)), NULL, CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2915713' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (325, N'7043', N'Gân chân bò', NULL, CAST(69.20 AS Decimal(5, 2)), 124, CAST(30.20 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), NULL, NULL, CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2915713' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (326, N'7044', N'Lưỡi bò', N'Beef tongue', CAST(73.20 AS Decimal(5, 2)), 164, CAST(13.60 AS Decimal(5, 2)), CAST(12.10 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), NULL, CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2915713' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (327, N'7045', N'Lưỡi lợn', N'Hog, tongue', CAST(70.80 AS Decimal(5, 2)), 178, CAST(14.20 AS Decimal(5, 2)), CAST(12.80 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), NULL, CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2915713' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (328, N'7046', N'Lòng lợn (ruột già)', N'Hog, intestine large raw', CAST(76.60 AS Decimal(5, 2)), 167, CAST(6.90 AS Decimal(5, 2)), CAST(15.10 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), NULL, CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2915713' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (329, N'7047', N'Lòng lợn (ruột non)', N'Hog intestine small raw without fat', CAST(90.50 AS Decimal(5, 2)), 44, CAST(7.20 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), NULL, CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2915713' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (330, N'7048', N'Mề gà', N'Chicken gizzard', CAST(75.60 AS Decimal(5, 2)), 99, CAST(21.30 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2915713' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (331, N'7049', N'óc bò', N'Brain beef', CAST(79.40 AS Decimal(5, 2)), 124, CAST(9.00 AS Decimal(5, 2)), CAST(9.50 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), NULL, CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (332, N'7050', N'óc lợn', N'Hog brain', CAST(80.10 AS Decimal(5, 2)), 123, CAST(9.00 AS Decimal(5, 2)), CAST(9.50 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (333, N'7051', N'Phổi bò', N'Beef, lung', CAST(79.10 AS Decimal(5, 2)), 103, CAST(15.20 AS Decimal(5, 2)), CAST(4.70 AS Decimal(5, 2)), NULL, NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (334, N'7052', N'Phổi lợn', N'Hog lung, raw', CAST(80.60 AS Decimal(5, 2)), 92, CAST(14.80 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), NULL, NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (335, N'7053', N'Sườn lợn', N'Pork, ribs', CAST(68.20 AS Decimal(5, 2)), 187, CAST(17.90 AS Decimal(5, 2)), CAST(12.80 AS Decimal(5, 2)), NULL, NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (336, N'7054', N'Tai lợn', N'Hog ears', CAST(72.90 AS Decimal(5, 2)), 126, CAST(21.00 AS Decimal(5, 2)), CAST(4.10 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), NULL, CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (337, N'7055', N'Tim bò', N'Beef, heart', CAST(80.40 AS Decimal(5, 2)), 89, CAST(15.00 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (338, N'7056', N'Tim gà', N'Chicken heart', CAST(77.50 AS Decimal(5, 2)), 114, CAST(16.00 AS Decimal(5, 2)), CAST(5.50 AS Decimal(5, 2)), NULL, NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (339, N'7057', N'Tim lợn', N'Hog heart', CAST(79.50 AS Decimal(5, 2)), 94, CAST(15.10 AS Decimal(5, 2)), CAST(3.20 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (340, N'7058', N'Tiết bò', N'Beef, blood', CAST(80.40 AS Decimal(5, 2)), 75, CAST(18.00 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (341, N'7059', N'Tiết lợn luộc', N'Hog blood, boiled', CAST(86.80 AS Decimal(5, 2)), 44, CAST(10.70 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), NULL, NULL, CAST(2.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (342, N'7060', N'Tiết lợn sống', N'Hog blood, raw', CAST(91.90 AS Decimal(5, 2)), 25, CAST(5.70 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), NULL, CAST(2.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (343, N'7061', N'Tủy xương bò', N'Beef bone marrow', CAST(8.70 AS Decimal(5, 2)), 814, CAST(1.10 AS Decimal(5, 2)), CAST(89.90 AS Decimal(5, 2)), NULL, NULL, CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (344, N'7062', N'Tủy xương lợn', N'Hog, bone marrow', CAST(15.00 AS Decimal(5, 2)), 749, CAST(2.30 AS Decimal(5, 2)), CAST(82.20 AS Decimal(5, 2)), NULL, NULL, CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2925694' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (345, N'7063', N'Ba tê', N'Pa tª', CAST(47.50 AS Decimal(5, 2)), 326, CAST(10.80 AS Decimal(5, 2)), CAST(24.60 AS Decimal(5, 2)), CAST(15.40 AS Decimal(5, 2)), NULL, CAST(1.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (346, N'7064', N'Chả lợn', N'Pork, mince fat meat grilled', CAST(32.60 AS Decimal(5, 2)), 517, CAST(10.80 AS Decimal(5, 2)), CAST(50.40 AS Decimal(5, 2)), CAST(5.10 AS Decimal(5, 2)), NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (347, N'7065', N'Chả quế lợn', N'Pork, cinnamon mince grilled', CAST(42.50 AS Decimal(5, 2)), 416, CAST(16.20 AS Decimal(5, 2)), CAST(39.00 AS Decimal(5, 2)), NULL, NULL, CAST(2.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (348, N'7066', N'Dăm bông lợn', N'Ham, pork', CAST(48.70 AS Decimal(5, 2)), 318, CAST(23.00 AS Decimal(5, 2)), CAST(25.00 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), NULL, CAST(3.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (349, N'7067', N'Dồi lợn', N'Blood mix-pudding with viscera in large intestine', CAST(78.90 AS Decimal(5, 2)), 114, CAST(12.40 AS Decimal(5, 2)), CAST(7.10 AS Decimal(5, 2)), NULL, NULL, CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (350, N'7068', N'Giò bò', N'Beef dumpling', CAST(48.80 AS Decimal(5, 2)), 357, CAST(13.80 AS Decimal(5, 2)), CAST(33.50 AS Decimal(5, 2)), NULL, NULL, CAST(3.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (351, N'7069', N'Giò lụa', N'Pork, mince lean meat steamed', CAST(72.00 AS Decimal(5, 2)), 136, CAST(21.50 AS Decimal(5, 2)), CAST(5.50 AS Decimal(5, 2)), NULL, NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (352, N'7070', N'Giò thủ lợn', N'Pork, head meat, steamed', CAST(29.00 AS Decimal(5, 2)), 553, CAST(16.00 AS Decimal(5, 2)), CAST(54.30 AS Decimal(5, 2)), NULL, NULL, CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (353, N'7071', N'Lạp xường', N'Chinese sausage', CAST(18.80 AS Decimal(5, 2)), 585, CAST(20.80 AS Decimal(5, 2)), CAST(55.00 AS Decimal(5, 2)), CAST(1.70 AS Decimal(5, 2)), NULL, CAST(3.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (354, N'7072', N'Nem chạo', NULL, CAST(68.10 AS Decimal(5, 2)), 153, CAST(16.60 AS Decimal(5, 2)), CAST(6.50 AS Decimal(5, 2)), CAST(6.90 AS Decimal(5, 2)), NULL, CAST(1.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (355, N'7073', N'Nem chua', N'Pork mince, fermented', CAST(68.00 AS Decimal(5, 2)), 137, CAST(21.70 AS Decimal(5, 2)), CAST(3.70 AS Decimal(5, 2)), CAST(4.30 AS Decimal(5, 2)), NULL, CAST(2.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (356, N'7074', N'Ruốc thịt lợn', NULL, CAST(25.80 AS Decimal(5, 2)), 369, CAST(46.60 AS Decimal(5, 2)), CAST(20.30 AS Decimal(5, 2)), NULL, NULL, CAST(7.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (357, N'7075', N'Thịt bò khô', N'Dried beef', CAST(32.40 AS Decimal(5, 2)), 239, CAST(51.00 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), CAST(5.20 AS Decimal(5, 2)), NULL, CAST(9.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (358, N'7076', N'Thịt trâu khô', N'Buffalo meat, dried', CAST(36.40 AS Decimal(5, 2)), 226, CAST(50.40 AS Decimal(5, 2)), CAST(2.70 AS Decimal(5, 2)), NULL, NULL, CAST(10.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (359, N'7077', N'Xúc xích', N'Pork sausage', CAST(17.00 AS Decimal(5, 2)), 535, CAST(27.20 AS Decimal(5, 2)), CAST(47.40 AS Decimal(5, 2)), NULL, NULL, CAST(8.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2935692' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (360, N'7078', N'Bột cóc', N'Toad meat powder', CAST(7.20 AS Decimal(5, 2)), 342, CAST(55.40 AS Decimal(5, 2)), CAST(13.40 AS Decimal(5, 2)), NULL, NULL, CAST(24.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2945683' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (361, N'7079', N'Châu chấu', N'Locust', CAST(71.10 AS Decimal(5, 2)), 130, CAST(24.30 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), NULL, NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2945683' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (362, N'7080', N'ếch (thịt đùi)', N'Frog', CAST(75.00 AS Decimal(5, 2)), 90, CAST(20.00 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), NULL, NULL, CAST(3.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2945683' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (363, N'7081', N'Nhộng', N'Silk worm', CAST(79.70 AS Decimal(5, 2)), 111, CAST(13.00 AS Decimal(5, 2)), CAST(6.50 AS Decimal(5, 2)), NULL, NULL, CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2945683' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (364, N'7082', N'Lòng gà (cả bộ)', N'Chicken giblets', CAST(74.90 AS Decimal(5, 2)), 119, CAST(17.90 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2945683' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (365, N'8001', N'Cá bống', N'Goby, gudgeon', CAST(81.90 AS Decimal(5, 2)), 70, CAST(15.80 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), NULL, NULL, CAST(1.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2945683' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (366, N'8002', N'Cá chày', N'Pond fish', CAST(75.10 AS Decimal(5, 2)), 113, CAST(20.10 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2945683' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (367, N'8003', N'Cá chép', N'Carp', CAST(79.10 AS Decimal(5, 2)), 96, CAST(16.00 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), NULL, NULL, CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2945683' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (368, N'8004', N'Cá dưa', N'Conger pike', CAST(75.60 AS Decimal(5, 2)), 115, CAST(17.60 AS Decimal(5, 2)), CAST(5.00 AS Decimal(5, 2)), NULL, NULL, CAST(1.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2945683' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (369, N'8005', N'Cá dầu', NULL, CAST(75.10 AS Decimal(5, 2)), 96, CAST(18.90 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), NULL, NULL, CAST(3.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2945683' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (370, N'8006', N'Cá diếc', N'Mullet, harder', CAST(78.90 AS Decimal(5, 2)), 87, CAST(17.70 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), NULL, NULL, CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2945683' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (371, N'8007', N'Cá đao', N'Sawfish, wolfherring', CAST(78.30 AS Decimal(5, 2)), 94, CAST(18.30 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), NULL, NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2945683' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (372, N'8008', N'Cá đé', N'Chinese herring', CAST(78.80 AS Decimal(5, 2)), 83, CAST(18.70 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), NULL, NULL, CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (373, N'8009', N'Cá đối', N'Mullet, gray mullet', CAST(76.00 AS Decimal(5, 2)), 108, CAST(19.50 AS Decimal(5, 2)), CAST(3.30 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (374, N'8010', N'Cá đồng tiền', N'Goby', CAST(76.40 AS Decimal(5, 2)), 98, CAST(20.10 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), NULL, NULL, CAST(1.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (375, N'8011', N'Cá hồi', N'Salmon', CAST(71.30 AS Decimal(5, 2)), 136, CAST(22.00 AS Decimal(5, 2)), CAST(5.30 AS Decimal(5, 2)), NULL, NULL, CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (376, N'8012', N'Cá khô (chim, thu, nụ, đé)', N'Dried fish, miscellaneous', CAST(37.80 AS Decimal(5, 2)), 208, CAST(43.30 AS Decimal(5, 2)), CAST(3.90 AS Decimal(5, 2)), NULL, NULL, CAST(15.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (377, N'8013', N'Cá lác', NULL, CAST(78.10 AS Decimal(5, 2)), 71, CAST(16.70 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), NULL, NULL, CAST(4.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (378, N'8014', N'Cá mè', N'Hypophthalmichthys, chub, dory, tench bream', CAST(74.70 AS Decimal(5, 2)), 144, CAST(15.40 AS Decimal(5, 2)), CAST(9.10 AS Decimal(5, 2)), NULL, NULL, CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (379, N'8015', N'Cá mòi', N'Sardin', CAST(75.30 AS Decimal(5, 2)), 124, CAST(17.50 AS Decimal(5, 2)), CAST(6.00 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (380, N'8016', N'Cá mỡ', NULL, CAST(72.70 AS Decimal(5, 2)), 151, CAST(16.80 AS Decimal(5, 2)), CAST(9.30 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (381, N'8017', N'Cá mối', NULL, CAST(73.30 AS Decimal(5, 2)), 116, CAST(22.10 AS Decimal(5, 2)), CAST(3.10 AS Decimal(5, 2)), NULL, NULL, CAST(1.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (382, N'8018', N'Cá nạc', N'Fish low fat', CAST(80.00 AS Decimal(5, 2)), 80, CAST(17.50 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), NULL, NULL, CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (383, N'8019', N'Cá ngư', N'Flying fish, tuna', CAST(77.50 AS Decimal(5, 2)), 87, CAST(21.00 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (384, N'8020', N'Cá nục', N'Scad, anchovy', CAST(74.90 AS Decimal(5, 2)), 111, CAST(20.20 AS Decimal(5, 2)), CAST(3.30 AS Decimal(5, 2)), NULL, NULL, CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (385, N'8021', N'Cá phèn', N'Goatfish, surmullet, red mullet', CAST(78.20 AS Decimal(5, 2)), 104, CAST(15.90 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), NULL, NULL, CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (386, N'8022', N'Cá quả', N'Fish, snake head', CAST(78.00 AS Decimal(5, 2)), 97, CAST(18.20 AS Decimal(5, 2)), CAST(2.70 AS Decimal(5, 2)), NULL, NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (387, N'8023', N'Cá rô đồng', N'Anabas', CAST(74.20 AS Decimal(5, 2)), 126, CAST(19.10 AS Decimal(5, 2)), CAST(5.50 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (388, N'8024', N'Cá rô phi', N'Tilapia, African carp', CAST(76.80 AS Decimal(5, 2)), 100, CAST(19.70 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (389, N'8025', N'Cá thờn bơn (cá bơn)', N'Flounder, sole, turbot', CAST(81.30 AS Decimal(5, 2)), 73, CAST(17.40 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), NULL, NULL, CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (390, N'8026', N'Cá thu', N'Mackerel, codfish, kingfish', CAST(70.20 AS Decimal(5, 2)), 166, CAST(18.20 AS Decimal(5, 2)), CAST(10.30 AS Decimal(5, 2)), NULL, NULL, CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (391, N'8027', N'Cá thu đao', NULL, CAST(70.30 AS Decimal(5, 2)), 156, CAST(20.00 AS Decimal(5, 2)), CAST(8.40 AS Decimal(5, 2)), NULL, NULL, CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (392, N'8028', N'Cá trạch (cá chạch)', N'Loach', CAST(74.20 AS Decimal(5, 2)), 110, CAST(20.40 AS Decimal(5, 2)), CAST(3.20 AS Decimal(5, 2)), NULL, NULL, CAST(2.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (393, N'8029', N'Cá trắm cỏ', N'Carp, amur', CAST(79.40 AS Decimal(5, 2)), 91, CAST(17.00 AS Decimal(5, 2)), CAST(2.60 AS Decimal(5, 2)), NULL, NULL, CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (394, N'8030', N'Cá trê', N'Catfish, silurus, sheatfish, hito', CAST(70.40 AS Decimal(5, 2)), 173, CAST(16.50 AS Decimal(5, 2)), CAST(11.90 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (395, N'8031', N'Cá trích', N'Herring', CAST(70.50 AS Decimal(5, 2)), 166, CAST(17.70 AS Decimal(5, 2)), CAST(10.60 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (396, N'8032', N'Cá trôi', N'Major carp, mud carp; cirrhina molitorella', CAST(74.30 AS Decimal(5, 2)), 127, CAST(18.80 AS Decimal(5, 2)), CAST(5.70 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (397, N'8033', N'Cua bể', N'Crab, sea water', CAST(72.20 AS Decimal(5, 2)), 103, CAST(17.50 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(7.00 AS Decimal(5, 2)), NULL, CAST(2.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2955786' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (398, N'8034', N'Cua đồng', N'Crab, fresh water', CAST(74.40 AS Decimal(5, 2)), 87, CAST(12.30 AS Decimal(5, 2)), CAST(3.30 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), NULL, CAST(8.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (399, N'8035', N'ghẹ', N'Crab', CAST(85.50 AS Decimal(5, 2)), 54, CAST(11.90 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), NULL, NULL, CAST(1.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (400, N'8036', N'Hải sâm', N'Sea slug, sea cucumber', CAST(76.90 AS Decimal(5, 2)), 90, CAST(21.50 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (401, N'8037', N'Hến', N'Clam', CAST(88.80 AS Decimal(5, 2)), 45, CAST(4.50 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(5.10 AS Decimal(5, 2)), NULL, CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (402, N'8038', N'Lươn', N'Eel, silver - pike', CAST(68.30 AS Decimal(5, 2)), 180, CAST(18.40 AS Decimal(5, 2)), CAST(11.70 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), NULL, CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (403, N'8039', N'Mữc khô', N'Dried cuttle fish, squid', CAST(26.20 AS Decimal(5, 2)), 291, CAST(60.10 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), NULL, CAST(6.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (404, N'8040', N'Mữc tươi', N'Cuttle fish, raw (Squid)', CAST(81.40 AS Decimal(5, 2)), 73, CAST(16.30 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), NULL, NULL, CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (405, N'8041', N'ốc bươu', N'Snail medium - size, edible', CAST(76.80 AS Decimal(5, 2)), 84, CAST(11.10 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(8.30 AS Decimal(5, 2)), NULL, CAST(3.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (406, N'8042', N'ốc đá', N'Marble snail, edible', CAST(80.30 AS Decimal(5, 2)), 63, CAST(11.20 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(3.90 AS Decimal(5, 2)), NULL, CAST(4.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (407, N'8043', N'ốc nhồi', N'Snail large, edible', CAST(77.60 AS Decimal(5, 2)), 84, CAST(11.90 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(7.60 AS Decimal(5, 2)), NULL, CAST(2.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (408, N'8044', N'ốc vặn', N'Helix', CAST(79.10 AS Decimal(5, 2)), 72, CAST(12.20 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(4.30 AS Decimal(5, 2)), NULL, CAST(3.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (409, N'8045', N'Rạm (muối, đồ)', N'Small sea - crab bicled, steamed', CAST(64.40 AS Decimal(5, 2)), 83, CAST(14.20 AS Decimal(5, 2)), CAST(2.90 AS Decimal(5, 2)), NULL, NULL, CAST(18.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (410, N'8046', N'Rạm tươi', N'Small sea - crab', CAST(78.20 AS Decimal(5, 2)), 77, CAST(12.90 AS Decimal(5, 2)), CAST(2.80 AS Decimal(5, 2)), NULL, NULL, CAST(6.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (411, N'8047', N'Rươi', N'Tylorhynchus sinensis', CAST(81.90 AS Decimal(5, 2)), 89, CAST(12.40 AS Decimal(5, 2)), CAST(4.40 AS Decimal(5, 2)), NULL, NULL, CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (412, N'8048', N'Sò', N'Oyster', CAST(82.10 AS Decimal(5, 2)), 78, CAST(9.50 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), CAST(4.90 AS Decimal(5, 2)), NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (413, N'8049', N'Tép gạo', N'Tiny shrimp', CAST(84.50 AS Decimal(5, 2)), 58, CAST(11.70 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), NULL, NULL, CAST(2.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (414, N'8050', N'Tép khô', N'Tiny shrimp, dried', CAST(23.00 AS Decimal(5, 2)), 269, CAST(59.80 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), NULL, CAST(13.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (415, N'8051', N'Tôm biển', N'Sea shrimp, sea-water shrimp', CAST(79.20 AS Decimal(5, 2)), 82, CAST(17.60 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), NULL, CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (416, N'8052', N'Tôm đồng', N'Fresh-water shrimp', CAST(76.90 AS Decimal(5, 2)), 90, CAST(18.40 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), NULL, NULL, CAST(2.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (417, N'8053', N'Tôm khô', N'Shrimp, dried', CAST(12.60 AS Decimal(5, 2)), 347, CAST(75.60 AS Decimal(5, 2)), CAST(3.80 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), NULL, CAST(5.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (418, N'8054', N'Trai', N'Manodonta', CAST(89.90 AS Decimal(5, 2)), 38, CAST(4.60 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), NULL, CAST(1.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (419, N'8055', N'Bánh phồng tôm rán', N'Deep fried shrimp paste', CAST(3.80 AS Decimal(5, 2)), 676, CAST(1.60 AS Decimal(5, 2)), CAST(59.20 AS Decimal(5, 2)), CAST(34.10 AS Decimal(5, 2)), NULL, CAST(1.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (420, N'8056', N'Bánh phồng tôm sống', N'Shrimp paste', CAST(12.00 AS Decimal(5, 2)), 381, CAST(3.40 AS Decimal(5, 2)), CAST(7.40 AS Decimal(5, 2)), CAST(75.30 AS Decimal(5, 2)), NULL, CAST(1.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (421, N'8057', N'Bột cá', N'Fish flour with bones', CAST(11.60 AS Decimal(5, 2)), 323, CAST(71.20 AS Decimal(5, 2)), CAST(2.90 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), NULL, CAST(11.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (422, N'8058', N'Ruốc cá quả', N'Shredded snake-head fish, salted and dried', CAST(14.00 AS Decimal(5, 2)), 312, CAST(65.70 AS Decimal(5, 2)), CAST(4.10 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), NULL, CAST(13.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (423, N'8059', N'Ruốc tôm', N'Shredded shrimp, salted and dried', CAST(13.10 AS Decimal(5, 2)), 305, CAST(65.50 AS Decimal(5, 2)), CAST(3.10 AS Decimal(5, 2)), CAST(3.70 AS Decimal(5, 2)), NULL, CAST(14.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (424, N'9001', N'Trứng gà', N'Hen egg, raw, whole', CAST(72.00 AS Decimal(5, 2)), 166, CAST(14.80 AS Decimal(5, 2)), CAST(11.60 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2965644' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (425, N'9002', N'Lòng đỏ trứng gà', N'Hen egg, yolk', CAST(54.00 AS Decimal(5, 2)), 327, CAST(13.60 AS Decimal(5, 2)), CAST(29.80 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), NULL, CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (426, N'9003', N'Lòng trắng trứng gà', N'Hen egg, white', CAST(88.00 AS Decimal(5, 2)), 46, CAST(10.30 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), NULL, CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (427, N'9004', N'Trứng vịt', N'Duck egg, whole', CAST(70.00 AS Decimal(5, 2)), 184, CAST(13.00 AS Decimal(5, 2)), CAST(14.20 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), NULL, CAST(1.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (428, N'9005', N'Lòng đỏ trứng vịt', N'Duck egg, yolk', CAST(47.50 AS Decimal(5, 2)), 364, CAST(13.60 AS Decimal(5, 2)), CAST(32.30 AS Decimal(5, 2)), CAST(4.80 AS Decimal(5, 2)), NULL, CAST(1.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (429, N'9006', N'Lòng trắng trứng vịt', N'Duck egg, white', CAST(87.80 AS Decimal(5, 2)), 47, CAST(10.70 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), NULL, CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (430, N'9007', N'Trứng chim cút', N'Quail egg', CAST(74.40 AS Decimal(5, 2)), 154, CAST(13.10 AS Decimal(5, 2)), CAST(11.10 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), NULL, CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (431, N'9008', N'Tên thữc phẩm (Vietnamese): 	Trứng cá', N'Tªn tiÕng Anh (English): 	Fish roe', CAST(64.90 AS Decimal(5, 2)), 171, CAST(20.50 AS Decimal(5, 2)), CAST(9.90 AS Decimal(5, 2)), NULL, NULL, CAST(4.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (432, N'9009', N'Trứng cá muối', N'Fish caviar, black and red, granule', CAST(47.50 AS Decimal(5, 2)), 274, CAST(24.60 AS Decimal(5, 2)), CAST(17.90 AS Decimal(5, 2)), CAST(3.50 AS Decimal(5, 2)), NULL, CAST(6.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (433, N'9010', N'Trứng vịt lộn', N'Duck egg, embryonated', CAST(67.00 AS Decimal(5, 2)), 182, CAST(13.60 AS Decimal(5, 2)), CAST(12.40 AS Decimal(5, 2)), CAST(4.00 AS Decimal(5, 2)), NULL, CAST(3.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (434, N'9011', N'Bột trứng', N'Chicken egg powder', CAST(8.50 AS Decimal(5, 2)), 563, CAST(44.00 AS Decimal(5, 2)), CAST(42.20 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), NULL, CAST(3.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (435, N'10001', N'Sữa bò tươi', N'Milk cow, fresh (fluid)', CAST(86.20 AS Decimal(5, 2)), 74, CAST(3.90 AS Decimal(5, 2)), CAST(4.40 AS Decimal(5, 2)), CAST(4.80 AS Decimal(5, 2)), NULL, CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (436, N'10002', N'Sữa dê tươi', N'Milk goat''s, whole', CAST(87.20 AS Decimal(5, 2)), 69, CAST(3.50 AS Decimal(5, 2)), CAST(4.10 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), NULL, CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (437, N'10003', N'Sữa mẹ (sữa người)', N'Breast milk (Human milk, whole).', CAST(88.30 AS Decimal(5, 2)), 61, CAST(1.50 AS Decimal(5, 2)), CAST(3.00 AS Decimal(5, 2)), CAST(7.00 AS Decimal(5, 2)), NULL, CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (438, N'10004', N'Sữa chua (từ sữa bò)', N'Yogurt (whole milk)', CAST(88.70 AS Decimal(5, 2)), 61, CAST(3.30 AS Decimal(5, 2)), CAST(3.70 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), NULL, CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (439, N'10005', N'Sữa chua vớt béo', N'Yogurt, chocolate, nonfat milk', CAST(71.60 AS Decimal(5, 2)), 103, CAST(3.50 AS Decimal(5, 2)), NULL, CAST(22.30 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (440, N'10006', N'Sữa bột toàn phần', N'Milk powder, whole', CAST(3.50 AS Decimal(5, 2)), 494, CAST(27.00 AS Decimal(5, 2)), CAST(26.00 AS Decimal(5, 2)), CAST(38.00 AS Decimal(5, 2)), NULL, CAST(5.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (441, N'10007', N'Sữa bột tách béo', N'Skimmed milk powder', CAST(4.00 AS Decimal(5, 2)), 357, CAST(35.00 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(52.00 AS Decimal(5, 2)), NULL, CAST(8.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (442, N'10008', N'Sữa đặc có đường Việt Nam', N'Milk condensed, sweetened', CAST(25.40 AS Decimal(5, 2)), 336, CAST(8.10 AS Decimal(5, 2)), CAST(8.80 AS Decimal(5, 2)), CAST(56.00 AS Decimal(5, 2)), NULL, CAST(1.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (443, N'10009', N'Pho mát', N'Cheese (whole fat)', CAST(38.90 AS Decimal(5, 2)), 380, CAST(25.50 AS Decimal(5, 2)), CAST(30.90 AS Decimal(5, 2)), NULL, NULL, CAST(4.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2980513' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (444, N'11001', N'Chuối nước đường', N'Banana, canned, sweetened', CAST(83.90 AS Decimal(5, 2)), 58, CAST(0.50 AS Decimal(5, 2)), NULL, CAST(13.90 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2990590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (445, N'11002', N'Dưa chuột hộp', N'Cucumber (canned)', CAST(94.10 AS Decimal(5, 2)), 8, CAST(0.30 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(3.10 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2990590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (446, N'11003', N'Dứa hộp', N'Pineapple, canned, sweetened', CAST(85.70 AS Decimal(5, 2)), 53, CAST(0.40 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(12.70 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2990590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (447, N'11004', N'Lạc chao dầu', N'Peanut, oil fried', CAST(1.80 AS Decimal(5, 2)), 680, CAST(25.70 AS Decimal(5, 2)), CAST(59.50 AS Decimal(5, 2)), CAST(10.30 AS Decimal(5, 2)), NULL, CAST(2.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2990590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (448, N'11005', N'Mắc coọc nước đường', N'Pyrus pachis in syrup', CAST(85.10 AS Decimal(5, 2)), 56, CAST(0.20 AS Decimal(5, 2)), NULL, CAST(13.70 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2990590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (449, N'11006', N'Mận nước đường', N'Plum, canned, sweetened', CAST(83.00 AS Decimal(5, 2)), 64, CAST(0.40 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(15.40 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2990590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (450, N'11007', N'Mứt bí ngô', N'Dried preserved squash', CAST(49.40 AS Decimal(5, 2)), 198, CAST(0.50 AS Decimal(5, 2)), NULL, CAST(49.10 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2990590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (451, N'11008', N'Mứt cam có vỏ', N'Orange marmalade, jellies', CAST(42.80 AS Decimal(5, 2)), 218, CAST(0.40 AS Decimal(5, 2)), NULL, CAST(54.20 AS Decimal(5, 2)), CAST(2.20 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2990590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (452, N'11009', N'Mứt chuối', N'Banana jam', CAST(43.80 AS Decimal(5, 2)), 218, CAST(0.70 AS Decimal(5, 2)), NULL, CAST(53.90 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2990590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (453, N'11010', N'Mứt dứa', N'Pineapple jam', CAST(47.20 AS Decimal(5, 2)), 208, CAST(0.50 AS Decimal(5, 2)), NULL, CAST(51.50 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.2990590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (454, N'11011', N'Tên thữc phẩm ( Vietnamese) 	Mứt đu đủ', N'Papaya jam', CAST(53.20 AS Decimal(5, 2)), 178, CAST(0.40 AS Decimal(5, 2)), NULL, CAST(44.10 AS Decimal(5, 2)), CAST(2.00 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (455, N'11012', N'Nhãn nước đường', N'Longan, canned, sweetened', CAST(83.30 AS Decimal(5, 2)), 62, CAST(0.50 AS Decimal(5, 2)), NULL, CAST(15.00 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (456, N'11013', N'Nước dứa hộp', N'Pineapple juice', CAST(86.40 AS Decimal(5, 2)), 53, CAST(0.40 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(12.60 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (457, N'11014', N'Vải nước đường', N'Litchi, canned, sweetened', CAST(83.60 AS Decimal(5, 2)), 60, CAST(0.40 AS Decimal(5, 2)), NULL, CAST(14.70 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (458, N'11015', N'Cá thu hộp', N'Mackerel, canned', CAST(57.50 AS Decimal(5, 2)), 207, CAST(24.80 AS Decimal(5, 2)), CAST(12.00 AS Decimal(5, 2)), NULL, NULL, CAST(5.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (459, N'11016', N'Cá trích hộp', N'Herring, canned', CAST(56.70 AS Decimal(5, 2)), 233, CAST(22.30 AS Decimal(5, 2)), CAST(14.40 AS Decimal(5, 2)), CAST(3.50 AS Decimal(5, 2)), NULL, CAST(3.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (460, N'11017', N'Thịt bò hộp', N'Beef, canned', CAST(61.40 AS Decimal(5, 2)), 251, CAST(16.40 AS Decimal(5, 2)), CAST(20.60 AS Decimal(5, 2)), NULL, NULL, CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (461, N'11018', N'Thịt gà hộp', N'Chicken, canned', CAST(59.00 AS Decimal(5, 2)), 273, CAST(17.00 AS Decimal(5, 2)), CAST(22.80 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (462, N'11019', N'Thịt lợn hộp', N'Pork, canned', CAST(49.00 AS Decimal(5, 2)), 344, CAST(17.30 AS Decimal(5, 2)), CAST(29.30 AS Decimal(5, 2)), CAST(2.70 AS Decimal(5, 2)), NULL, CAST(1.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (463, N'11020', N'Thịt lợn, thịt bò xay hộp', N'Pork and beef, minced canned', CAST(62.80 AS Decimal(5, 2)), 244, CAST(16.00 AS Decimal(5, 2)), CAST(20.00 AS Decimal(5, 2)), NULL, NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (464, N'11021', N'Thịt vịt hầm', N'Duck, stewed meat', CAST(59.90 AS Decimal(5, 2)), 224, CAST(19.60 AS Decimal(5, 2)), CAST(16.20 AS Decimal(5, 2)), NULL, NULL, CAST(4.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (465, N'12001', N'Bánh bích cốt', N'Whole wheat rusk', CAST(12.00 AS Decimal(5, 2)), 346, CAST(12.30 AS Decimal(5, 2)), CAST(1.30 AS Decimal(5, 2)), CAST(71.30 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(2.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (466, N'12002', N'Bánh bích quy', N'Biscuits', CAST(10.40 AS Decimal(5, 2)), 376, CAST(8.80 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), CAST(75.10 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (467, N'12003', N'Bánh chả', N'Sweet meat pie in lumps', CAST(8.00 AS Decimal(5, 2)), 395, CAST(3.40 AS Decimal(5, 2)), CAST(6.60 AS Decimal(5, 2)), CAST(80.50 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3003590' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (468, N'12004', N'Bánh con cá', N'Small biscuits, fish figured', CAST(10.00 AS Decimal(5, 2)), 368, CAST(7.50 AS Decimal(5, 2)), CAST(2.20 AS Decimal(5, 2)), CAST(79.60 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3013652' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (469, N'12005', N'Bánh đậu xanh', N'Mungbean cake', CAST(6.80 AS Decimal(5, 2)), 416, CAST(15.60 AS Decimal(5, 2)), CAST(11.50 AS Decimal(5, 2)), CAST(62.40 AS Decimal(5, 2)), CAST(1.10 AS Decimal(5, 2)), CAST(2.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3013652' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (470, N'12006', N'Bánh kem xốp', N'Wafers filled', CAST(1.90 AS Decimal(5, 2)), 492, CAST(8.30 AS Decimal(5, 2)), CAST(24.00 AS Decimal(5, 2)), CAST(60.70 AS Decimal(5, 2)), CAST(3.50 AS Decimal(5, 2)), CAST(1.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3013652' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (471, N'12007', N'Bánh khảo chay', N'Rice cake, plain', CAST(5.80 AS Decimal(5, 2)), 376, CAST(3.20 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(90.20 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3013652' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (472, N'12008', N'Bánh quế', N'Cinnamon waffle (roll)', CAST(3.60 AS Decimal(5, 2)), 435, CAST(8.30 AS Decimal(5, 2)), CAST(10.70 AS Decimal(5, 2)), CAST(76.40 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3013652' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (473, N'12009', N'Bánh sô cô la', N'Chocolate, filled', CAST(8.50 AS Decimal(5, 2)), 449, CAST(3.90 AS Decimal(5, 2)), CAST(17.60 AS Decimal(5, 2)), CAST(68.80 AS Decimal(5, 2)), NULL, CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3013652' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (474, N'12010', N'Bánh thỏi sô cô la', N'Chocolate, sweet', CAST(0.80 AS Decimal(5, 2)), 543, CAST(4.90 AS Decimal(5, 2)), CAST(30.40 AS Decimal(5, 2)), CAST(62.50 AS Decimal(5, 2)), NULL, CAST(1.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3013652' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (475, N'12011', N'Bánh trứng nhện', N'Small biscuit', CAST(11.60 AS Decimal(5, 2)), 369, CAST(9.60 AS Decimal(5, 2)), CAST(3.70 AS Decimal(5, 2)), CAST(74.20 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3013652' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (476, N'12012', N'Bột ca cao', N'Cocoa powder', CAST(3.00 AS Decimal(5, 2)), 414, CAST(19.60 AS Decimal(5, 2)), CAST(13.70 AS Decimal(5, 2)), CAST(53.00 AS Decimal(5, 2)), CAST(4.60 AS Decimal(5, 2)), CAST(6.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3013652' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (477, N'12013', N'Đường cát', N'Refined sugar', CAST(1.70 AS Decimal(5, 2)), 390, NULL, NULL, CAST(97.40 AS Decimal(5, 2)), NULL, CAST(0.90 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3013652' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (478, N'12014', N'Đường kính', N'Granulated sugar', CAST(0.50 AS Decimal(5, 2)), 397, NULL, NULL, CAST(99.30 AS Decimal(5, 2)), NULL, CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3013652' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (479, N'12015', N'Kẹo bơ cứng', N'Toffee, sweets', CAST(8.80 AS Decimal(5, 2)), 448, CAST(2.10 AS Decimal(5, 2)), CAST(17.20 AS Decimal(5, 2)), CAST(71.10 AS Decimal(5, 2)), NULL, CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3013652' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (480, N'12016', N'Kẹo cà phê', N'Coffee sweets', CAST(6.60 AS Decimal(5, 2)), 378, NULL, CAST(1.30 AS Decimal(5, 2)), CAST(91.50 AS Decimal(5, 2)), NULL, CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3023649' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (481, N'12017', N'Kẹo cam chanh', N'Sweets, caramel with citron, orange aroma', CAST(5.40 AS Decimal(5, 2)), 377, NULL, CAST(0.50 AS Decimal(5, 2)), CAST(93.10 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3023649' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (482, N'12018', N'Kẹo dưa mềm', N'Sweets with coconut aroma', CAST(8.50 AS Decimal(5, 2)), 415, CAST(0.60 AS Decimal(5, 2)), CAST(12.20 AS Decimal(5, 2)), CAST(75.60 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3023649' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (483, N'12019', N'Kẹo dứa mềm', N'Pineapple sweets candy', CAST(8.50 AS Decimal(5, 2)), 415, CAST(0.60 AS Decimal(5, 2)), CAST(12.20 AS Decimal(5, 2)), CAST(75.60 AS Decimal(5, 2)), CAST(2.50 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3023649' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (484, N'12020', N'Kẹo lạc', N'Peanut candy', CAST(5.00 AS Decimal(5, 2)), 449, CAST(10.30 AS Decimal(5, 2)), CAST(16.50 AS Decimal(5, 2)), CAST(64.80 AS Decimal(5, 2)), CAST(2.20 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3023649' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (485, N'12021', N'Kẹo Pastille (kẹo ngậm bạc hà)', N'Menthol pastille', CAST(10.20 AS Decimal(5, 2)), 356, CAST(5.20 AS Decimal(5, 2)), NULL, CAST(83.80 AS Decimal(5, 2)), NULL, CAST(0.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3023649' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (486, N'12022', N'Kẹo sô cô la', N'Sweets with chocolate', CAST(6.80 AS Decimal(5, 2)), 388, CAST(1.60 AS Decimal(5, 2)), CAST(4.60 AS Decimal(5, 2)), CAST(85.10 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3023649' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (487, N'12023', N'Kẹo sữa', N'Sweets with milk', CAST(8.40 AS Decimal(5, 2)), 390, CAST(2.90 AS Decimal(5, 2)), CAST(5.20 AS Decimal(5, 2)), CAST(83.00 AS Decimal(5, 2)), NULL, CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3023649' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (488, N'12024', N'Kẹo vưng viên', N'Sesame candy', CAST(1.70 AS Decimal(5, 2)), 417, CAST(2.80 AS Decimal(5, 2)), CAST(6.90 AS Decimal(5, 2)), CAST(85.90 AS Decimal(5, 2)), CAST(1.70 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3023649' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (489, N'12025', N'Mạch nha', N'Malt', CAST(15.00 AS Decimal(5, 2)), 331, CAST(0.20 AS Decimal(5, 2)), NULL, CAST(82.50 AS Decimal(5, 2)), NULL, CAST(2.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3023649' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (490, N'12026', N'Mật ong', N'Honey', CAST(18.00 AS Decimal(5, 2)), 327, CAST(0.40 AS Decimal(5, 2)), NULL, CAST(81.30 AS Decimal(5, 2)), NULL, CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (491, N'12027', N'Mứt lạc', N'Peanut sugar (Jam)', CAST(1.80 AS Decimal(5, 2)), 431, CAST(5.40 AS Decimal(5, 2)), CAST(8.60 AS Decimal(5, 2)), CAST(82.90 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (492, N'13001', N'Cary bột', N'Cari powder (Mix, turmeric, red pepper and other spices)', CAST(10.00 AS Decimal(5, 2)), 283, CAST(8.20 AS Decimal(5, 2)), CAST(7.30 AS Decimal(5, 2)), CAST(46.00 AS Decimal(5, 2)), CAST(8.90 AS Decimal(5, 2)), CAST(19.60 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (493, N'13002', N'Gưng khô (bột)', N'Ginger root, dried powder', CAST(9.40 AS Decimal(5, 2)), 323, CAST(9.10 AS Decimal(5, 2)), CAST(5.90 AS Decimal(5, 2)), CAST(58.30 AS Decimal(5, 2)), CAST(12.50 AS Decimal(5, 2)), CAST(4.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (494, N'13003', N'Gưng tươi', N'Ginger root, fresh', CAST(90.00 AS Decimal(5, 2)), 29, CAST(0.40 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(5.10 AS Decimal(5, 2)), CAST(3.30 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (495, N'13004', N'Hạt tiêu', N'Peppercorn, seeds', CAST(13.50 AS Decimal(5, 2)), 231, CAST(7.00 AS Decimal(5, 2)), CAST(7.40 AS Decimal(5, 2)), CAST(34.10 AS Decimal(5, 2)), CAST(33.50 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (496, N'13005', N'Muối', N'Table salt', CAST(1.00 AS Decimal(5, 2)), NULL, NULL, NULL, NULL, NULL, CAST(99.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (497, N'13006', N'Nghệ khô, bột', N'Turmeric rhizome, dried powder', CAST(11.40 AS Decimal(5, 2)), 295, CAST(7.80 AS Decimal(5, 2)), CAST(9.90 AS Decimal(5, 2)), CAST(43.80 AS Decimal(5, 2)), CAST(21.10 AS Decimal(5, 2)), CAST(6.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (498, N'13007', N'Nghệ tươi', N'Turmeric, rhizome, fresh', CAST(86.70 AS Decimal(5, 2)), 25, CAST(1.10 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), CAST(4.40 AS Decimal(5, 2)), CAST(6.50 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (499, N'13008', N'ớt khô bột', N'Red pepper, dried powder', CAST(12.00 AS Decimal(5, 2)), 227, CAST(15.60 AS Decimal(5, 2)), CAST(4.20 AS Decimal(5, 2)), CAST(31.80 AS Decimal(5, 2)), CAST(23.60 AS Decimal(5, 2)), CAST(12.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (500, N'13009', N'Riềng', N'Alpinia root, fresh', CAST(91.70 AS Decimal(5, 2)), 26, CAST(0.30 AS Decimal(5, 2)), NULL, CAST(2.50 AS Decimal(5, 2)), CAST(3.70 AS Decimal(5, 2)), CAST(1.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (501, N'13010', N'Magi', N'Soybean sauce', CAST(66.00 AS Decimal(5, 2)), 65, CAST(10.50 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(5.60 AS Decimal(5, 2)), NULL, CAST(17.80 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (502, N'13011', N'Mắm tôm đặc', N'Shrimp sauce concentrate', CAST(48.00 AS Decimal(5, 2)), 73, CAST(14.80 AS Decimal(5, 2)), CAST(1.50 AS Decimal(5, 2)), NULL, NULL, CAST(35.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (503, N'13012', N'Mắm tôm loãng', N'Shrimp sauce, diluted', CAST(68.60 AS Decimal(5, 2)), 44, CAST(7.00 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(2.10 AS Decimal(5, 2)), NULL, CAST(21.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3033641' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (504, N'13013', N'Mắm tép chua', N'Tiny shrimp, sour sauce', CAST(55.50 AS Decimal(5, 2)), 68, CAST(8.70 AS Decimal(5, 2)), CAST(1.20 AS Decimal(5, 2)), CAST(5.50 AS Decimal(5, 2)), NULL, CAST(29.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3043643' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (505, N'13014', N'Nước mắm cá (loại đặc biệt)', N'Fish - sauce (super quality).', CAST(60.00 AS Decimal(5, 2)), 60, CAST(15.00 AS Decimal(5, 2)), NULL, NULL, NULL, CAST(25.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3043643' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (506, N'13015', N'Tên thữc phẩm (Vietnamese): 	Nước mắm cá loại I', N'Tªn tiÕng Anh (English): 	Fish sauce, grade I', CAST(67.90 AS Decimal(5, 2)), 28, CAST(7.10 AS Decimal(5, 2)), NULL, NULL, NULL, CAST(25.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3043643' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (507, N'13016', N'Tên thữc phẩm (Vietnamese): 	Nước mắm loại II', N'Tªn tiÕng Anh (English): 	Fish sauce, grade II', CAST(66.80 AS Decimal(5, 2)), 21, CAST(5.20 AS Decimal(5, 2)), NULL, NULL, NULL, CAST(28.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3043643' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (508, N'13017', N'Nước mắm cá', N'Fish sauce, ready-to-serve', CAST(71.10 AS Decimal(5, 2)), 35, CAST(5.10 AS Decimal(5, 2)), CAST(0.01 AS Decimal(5, 2)), CAST(3.60 AS Decimal(5, 2)), NULL, CAST(20.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3043643' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (509, N'13018', N'Nước mắm cô', N'Fish sauce, concentrated', CAST(20.20 AS Decimal(5, 2)), 131, CAST(32.80 AS Decimal(5, 2)), NULL, NULL, NULL, CAST(47.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3043643' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (510, N'13019', N'Tương ngô', N'Soybean sauce with rice and maize', CAST(70.30 AS Decimal(5, 2)), 75, CAST(3.90 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), CAST(14.50 AS Decimal(5, 2)), NULL, CAST(11.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3043643' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (511, N'13020', N'Tương nếp', N'Soybean sauce with glutinous rice', CAST(68.30 AS Decimal(5, 2)), 86, CAST(4.30 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), CAST(15.70 AS Decimal(5, 2)), NULL, CAST(11.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3043643' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (512, N'13021', N'Tương ớt', N'Red pepper sauce concentrate', CAST(84.00 AS Decimal(5, 2)), 37, CAST(0.50 AS Decimal(5, 2)), CAST(0.50 AS Decimal(5, 2)), CAST(7.60 AS Decimal(5, 2)), CAST(0.90 AS Decimal(5, 2)), CAST(6.50 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3043643' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (513, N'13022', N'Xì dầu', N'Soybean sauce', CAST(70.80 AS Decimal(5, 2)), 53, CAST(6.30 AS Decimal(5, 2)), CAST(0.04 AS Decimal(5, 2)), CAST(6.80 AS Decimal(5, 2)), CAST(0.80 AS Decimal(5, 2)), CAST(15.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3043643' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (514, N'13023', N'Sốt mayonnaise', N'Mayonnaise', CAST(21.70 AS Decimal(5, 2)), 701, NULL, CAST(77.80 AS Decimal(5, 2)), CAST(0.10 AS Decimal(5, 2)), NULL, CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3043643' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (515, N'14001', N'Bia (cồn: 4,5 g)', N'Beer light (4,5 % Alcohol)', CAST(92.50 AS Decimal(5, 2)), 11, CAST(0.50 AS Decimal(5, 2)), NULL, CAST(2.30 AS Decimal(5, 2)), NULL, CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3043643' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (516, N'14002', N'Cô nhắc (cồn 32 g)', N'Cognac', CAST(67.80 AS Decimal(5, 2)), NULL, NULL, NULL, NULL, NULL, CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (517, N'14003', N'Cốc tai (cồn 13 g)', N'Cocktail, vermouth sweet', CAST(71.00 AS Decimal(5, 2)), 63, NULL, NULL, CAST(15.70 AS Decimal(5, 2)), NULL, CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (518, N'14004', N'Coca cola', N'Coca Cola', CAST(89.40 AS Decimal(5, 2)), 42, NULL, NULL, CAST(10.40 AS Decimal(5, 2)), NULL, CAST(0.20 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (519, N'14005', N'Nước cam tươi', N'Orange juice, fresh', CAST(94.10 AS Decimal(5, 2)), 23, CAST(0.70 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(4.50 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (520, N'14006', N'Nước dưa non tươi', N'Coconut milk, immature', CAST(94.40 AS Decimal(5, 2)), 21, CAST(0.40 AS Decimal(5, 2)), NULL, CAST(4.80 AS Decimal(5, 2)), NULL, CAST(0.40 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (521, N'14007', N'Nước ép cà chua', N'Tomato juice', CAST(94.60 AS Decimal(5, 2)), 19, CAST(0.80 AS Decimal(5, 2)), CAST(0.60 AS Decimal(5, 2)), CAST(2.60 AS Decimal(5, 2)), CAST(0.40 AS Decimal(5, 2)), CAST(1.00 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (522, N'14008', N'Nước khoáng', N'Mineral water', CAST(99.70 AS Decimal(5, 2)), NULL, NULL, NULL, NULL, NULL, CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (523, N'14009', N'Nước quít tươi', N'Mandarins juice (fresh)', CAST(93.80 AS Decimal(5, 2)), 24, CAST(0.40 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(5.10 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (524, N'14010', N'Rượu cam, chanh (cồn 24,2 g)', N'Liqueur, orange flavor', CAST(75.80 AS Decimal(5, 2)), NULL, NULL, NULL, NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (525, N'14011', N'Rượu nếp (80g/ 24 ml) (cồn 5 g)', N'Rice glutinous (Alcohol fermentation rice 80 g water 24 ml)', CAST(52.40 AS Decimal(5, 2)), 167, CAST(4.00 AS Decimal(5, 2)), NULL, CAST(37.70 AS Decimal(5, 2)), CAST(0.20 AS Decimal(5, 2)), CAST(0.70 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (526, N'14012', N'Rượu trắng (cồn 39 g)', N'Vodka', CAST(61.00 AS Decimal(5, 2)), 273, NULL, NULL, NULL, NULL, NULL, 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (527, N'14013', N'Rượu vang đỏ (cồn 9,5 g)', N'Red wine', CAST(88.00 AS Decimal(5, 2)), 9, CAST(0.20 AS Decimal(5, 2)), NULL, CAST(2.00 AS Decimal(5, 2)), NULL, CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (528, N'14014', N'Rượu vang trắng (cồn 9,5 g)', N'White wine', CAST(89.90 AS Decimal(5, 2)), 1, CAST(0.10 AS Decimal(5, 2)), NULL, CAST(0.20 AS Decimal(5, 2)), NULL, CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (529, N'14015', N'Rượu vang trắng ngọt (cồn 10.2 g)', N'White wine, sweet', CAST(83.40 AS Decimal(5, 2)), 24, CAST(0.20 AS Decimal(5, 2)), NULL, CAST(5.90 AS Decimal(5, 2)), NULL, CAST(0.30 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
INSERT [dbo].[foods_dictionary] ([id], [food_code], [food_name], [english_name], [water_g], [energy_kcal], [protein_g], [lipid_g], [glucid_g], [celluloza_g], [ash_g], [is_active], [created_at]) VALUES (530, N'14016', N'Rượu Whisky (cồn 35,2 g)', N'Whisky liqueur', CAST(64.70 AS Decimal(5, 2)), NULL, NULL, NULL, NULL, NULL, CAST(0.10 AS Decimal(5, 2)), 1, CAST(N'2026-06-28T22:22:03.3053659' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[foods_dictionary] OFF
GO
SET IDENTITY_INSERT [dbo].[hospitals] ON 
GO
INSERT [dbo].[hospitals] ([id], [account_id], [hospital_code], [full_name], [address], [phone], [is_active], [created_at], [updated_at]) VALUES (1, 1, N'BV001', N'Bệnh viện Đa khoa Trung ương Mẫu', N'123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội', N'024-3869-3731', 1, CAST(N'2026-06-27T11:31:39.0938819' AS DateTime2), CAST(N'2026-06-27T11:31:39.0938819' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[hospitals] OFF
GO
SET IDENTITY_INSERT [dbo].[medication_logs] ON 
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (15, 1, CAST(N'2026-06-21' AS Date), 0, NULL)
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (13, 1, CAST(N'2026-06-22' AS Date), 1, CAST(N'2026-06-22T08:03:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (11, 1, CAST(N'2026-06-23' AS Date), 1, CAST(N'2026-06-23T08:06:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (9, 1, CAST(N'2026-06-24' AS Date), 0, NULL)
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (7, 1, CAST(N'2026-06-25' AS Date), 1, CAST(N'2026-06-25T08:01:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (5, 1, CAST(N'2026-06-26' AS Date), 1, CAST(N'2026-06-26T08:02:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (3, 1, CAST(N'2026-06-27' AS Date), 1, CAST(N'2026-06-27T08:05:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (1, 1, CAST(N'2026-06-28' AS Date), 1, CAST(N'2026-06-28T16:31:36.8952914' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (19, 1, CAST(N'2026-07-12' AS Date), 1, CAST(N'2026-07-12T10:09:31.7456878' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (16, 2, CAST(N'2026-06-21' AS Date), 0, NULL)
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (14, 2, CAST(N'2026-06-22' AS Date), 0, NULL)
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (12, 2, CAST(N'2026-06-23' AS Date), 1, CAST(N'2026-06-23T08:10:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (10, 2, CAST(N'2026-06-24' AS Date), 1, CAST(N'2026-06-24T08:15:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (8, 2, CAST(N'2026-06-25' AS Date), 1, CAST(N'2026-06-25T08:04:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (6, 2, CAST(N'2026-06-26' AS Date), 0, NULL)
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (4, 2, CAST(N'2026-06-27' AS Date), 1, CAST(N'2026-06-27T08:12:00.0000000' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (2, 2, CAST(N'2026-06-28' AS Date), 1, CAST(N'2026-06-28T17:02:48.1738810' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (20, 2, CAST(N'2026-07-12' AS Date), 1, CAST(N'2026-07-12T10:09:32.7549221' AS DateTime2))
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (17, 3, CAST(N'2026-06-28' AS Date), 0, NULL)
GO
INSERT [dbo].[medication_logs] ([id], [patient_medication_id], [log_date], [is_taken], [taken_at]) VALUES (18, 5, CAST(N'2026-06-28' AS Date), 0, NULL)
GO
SET IDENTITY_INSERT [dbo].[medication_logs] OFF
GO
SET IDENTITY_INSERT [dbo].[notifications] ON 
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (146, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_4', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: ha (5 gói) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-06-28T16:59:19.6345463' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (147, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_5', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: dc (5h) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-06-28T16:59:19.6815915' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (148, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_7', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: sa (3 b) lúc 16:52 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-06-28T16:59:19.7451751' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (149, N'PATIENT', 1, N'UPCOMING_MED_REMINDER_8', N'Nhắc nhở uống thuốc sắp tới', N'Sắp đến giờ uống thuốc: test1 (2b) lúc 17:01.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-06-28T17:00:03.4252104' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (150, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_8', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: test1 (2b) lúc 17:01 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-06-28T17:02:43.4805226' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (151, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_1', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: tieu duong (1 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-29T08:03:42.3961191' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (152, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_2', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: huyết áp (2 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-29T08:03:42.5505287' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (153, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_4', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: ha (5 gói) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-29T08:03:42.5655252' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (154, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_5', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: dc (5h) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-06-29T08:03:42.5784755' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (155, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_1', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: tieu duong (1 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-01T22:30:59.4542253' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (156, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_2', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: huyết áp (2 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-01T22:30:59.6785607' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (157, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_4', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: ha (5 gói) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-01T22:30:59.6995210' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (158, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_5', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: dc (5h) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-01T22:30:59.7272742' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (159, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_8', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: test1 (2b) lúc 17:01 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-01T22:30:59.7524681' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (160, N'PATIENT', 1, N'HEALTH_LOG_REMINDER', N'Nhắc nhở nhập chỉ số sức khỏe', N'Đã quá 20:00 nhưng bạn chưa nhập chỉ số sức khỏe (huyết áp, đường huyết...) cho hôm nay. Vui lòng ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-01T22:30:59.7873846' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (161, N'PATIENT', 1, N'UPCOMING_MED_REMINDER_1', N'Nhắc nhở uống thuốc sắp tới', N'Bạn có lịch hẹn uống thuốc: tieu duong (1 viên) lúc 08:00. Vui lòng chuẩn bị và ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-03T07:46:23.5157876' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (162, N'PATIENT', 1, N'UPCOMING_MED_REMINDER_2', N'Nhắc nhở uống thuốc sắp tới', N'Bạn có lịch hẹn uống thuốc: huyết áp (2 viên) lúc 08:00. Vui lòng chuẩn bị và ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-03T07:46:23.6689714' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (163, N'PATIENT', 1, N'UPCOMING_MED_REMINDER_4', N'Nhắc nhở uống thuốc sắp tới', N'Bạn có lịch hẹn uống thuốc: ha (5 gói) lúc 08:00. Vui lòng chuẩn bị và ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-03T07:46:23.6788855' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (164, N'PATIENT', 1, N'UPCOMING_MED_REMINDER_5', N'Nhắc nhở uống thuốc sắp tới', N'Bạn có lịch hẹn uống thuốc: dc (5h) lúc 08:00. Vui lòng chuẩn bị và ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-03T07:46:23.6889740' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (165, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_1', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: tieu duong (1 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-03T08:28:06.4335111' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (166, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_2', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: huyết áp (2 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-03T08:28:06.4639808' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (167, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_4', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: ha (5 gói) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-03T08:28:06.4739814' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (168, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_5', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: dc (5h) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-03T08:28:06.4879799' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (169, N'PATIENT', 2, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa sáng hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T10:04:32.0681698' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (172, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_4', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: ha (5 gói) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T10:04:43.8109431' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (173, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_5', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: dc (5h) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T10:04:43.8312231' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (174, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa sáng hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T10:04:43.8640231' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (175, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa trưa hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T14:58:21.4316526' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (176, N'PATIENT', 1, N'UPCOMING_MED_REMINDER_8', N'Nhắc nhở uống thuốc sắp tới', N'Bạn có lịch hẹn uống thuốc: test1 (2b) lúc 17:01. Vui lòng chuẩn bị và ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T16:32:50.8266175' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (177, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_8', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: test1 (2b) lúc 17:01 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T17:17:31.9621864' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (178, N'PATIENT', 1, N'EXERCISE_REMINDER', N'Nhắc nhở tập luyện', N'Bạn chưa ghi nhận vận động hôm nay. Hãy dành ít phút hoạt động để đạt mục tiêu sức khỏe nhé!', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-12T20:13:41.4155399' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (179, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa tối hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 1, NULL, NULL, CAST(N'2026-07-12T21:29:31.2711759' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (180, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_1', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: tieu duong (1 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T08:33:07.7333518' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (181, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_2', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: huyết áp (2 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T08:33:07.8659817' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (182, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_4', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: ha (5 gói) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T08:33:07.8891031' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (183, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_5', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: dc (5h) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T08:33:07.9030671' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (184, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa sáng hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T09:15:11.2075771' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (186, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_8', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: test1 (2b) lúc 17:01 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T18:56:47.1879338' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (187, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa trưa hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T18:56:47.3720719' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (188, N'PATIENT', 1, N'HEALTH_LOG_REMINDER', N'Nhắc nhở nhập chỉ số sức khỏe', N'Đã quá 20:00 nhưng bạn chưa nhập chỉ số sức khỏe (huyết áp, đường huyết...) cho hôm nay. Vui lòng ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T21:32:42.9335787' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (189, N'PATIENT', 1, N'EXERCISE_REMINDER', N'Nhắc nhở tập luyện', N'Bạn chưa ghi nhận vận động hôm nay. Hãy dành ít phút hoạt động để đạt mục tiêu sức khỏe nhé!', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T21:32:43.0230245' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (190, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa tối hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T21:32:43.2150450' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (191, N'PATIENT', 2, N'HEALTH_LOG_REMINDER', N'Nhắc nhở nhập chỉ số sức khỏe', N'Đã quá 20:00 nhưng bạn chưa nhập chỉ số sức khỏe (huyết áp, đường huyết...) cho hôm nay. Vui lòng ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T22:37:30.6129540' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (192, N'PATIENT', 2, N'EXERCISE_REMINDER', N'Nhắc nhở tập luyện', N'Bạn chưa ghi nhận vận động hôm nay. Hãy dành ít phút hoạt động để đạt mục tiêu sức khỏe nhé!', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T22:37:30.6456068' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (193, N'PATIENT', 2, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa sáng hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T22:37:30.6883425' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (194, N'PATIENT', 2, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa trưa hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T22:37:30.7063688' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (195, N'PATIENT', 2, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa tối hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T22:37:30.7145848' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (196, N'PATIENT', 3, N'HEALTH_LOG_REMINDER', N'Nhắc nhở nhập chỉ số sức khỏe', N'Đã quá 20:00 nhưng bạn chưa nhập chỉ số sức khỏe (huyết áp, đường huyết...) cho hôm nay. Vui lòng ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T22:42:55.8961015' AS DateTime2), 3, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (197, N'PATIENT', 3, N'EXERCISE_REMINDER', N'Nhắc nhở tập luyện', N'Bạn chưa ghi nhận vận động hôm nay. Hãy dành ít phút hoạt động để đạt mục tiêu sức khỏe nhé!', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T22:42:55.9294747' AS DateTime2), 3, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (198, N'PATIENT', 3, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa sáng hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T22:42:55.9484801' AS DateTime2), 3, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (199, N'PATIENT', 3, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa trưa hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T22:42:55.9614186' AS DateTime2), 3, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (200, N'PATIENT', 3, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa tối hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-13T22:42:55.9796270' AS DateTime2), 3, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (201, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_1', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: tieu duong (1 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T08:41:15.4767286' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (202, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_2', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: huyết áp (2 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T08:41:15.5742599' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (203, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_4', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: ha (5 gói) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T08:41:15.6072602' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (204, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_5', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: dc (5h) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T08:41:15.6162719' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (205, N'PATIENT', 2, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa sáng hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-14T09:17:20.0513720' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (206, N'PATIENT', 2, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa sáng hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-15T16:00:58.5952466' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (207, N'PATIENT', 2, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa trưa hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-15T16:00:58.6573745' AS DateTime2), 2, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (208, N'PATIENT', 1, N'UPCOMING_MED_REMINDER_1', N'Nhắc nhở uống thuốc sắp tới', N'Bạn có lịch hẹn uống thuốc: tieu duong (1 viên) lúc 08:00. Vui lòng chuẩn bị và ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T07:44:28.7291890' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (209, N'PATIENT', 1, N'UPCOMING_MED_REMINDER_2', N'Nhắc nhở uống thuốc sắp tới', N'Bạn có lịch hẹn uống thuốc: huyết áp (2 viên) lúc 08:00. Vui lòng chuẩn bị và ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T07:44:28.8021485' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (210, N'PATIENT', 1, N'UPCOMING_MED_REMINDER_4', N'Nhắc nhở uống thuốc sắp tới', N'Bạn có lịch hẹn uống thuốc: ha (5 gói) lúc 08:00. Vui lòng chuẩn bị và ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T07:44:28.8061380' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (211, N'PATIENT', 1, N'UPCOMING_MED_REMINDER_5', N'Nhắc nhở uống thuốc sắp tới', N'Bạn có lịch hẹn uống thuốc: dc (5h) lúc 08:00. Vui lòng chuẩn bị và ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T07:44:28.8091487' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (212, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_1', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: tieu duong (1 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:02:30.9071851' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (213, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_2', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: huyết áp (2 viên) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:02:31.0053425' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (214, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_4', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: ha (5 gói) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:02:31.0203654' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (215, N'PATIENT', 1, N'OVERDUE_MED_REMINDER_5', N'Nhắc nhở quá giờ uống thuốc', N'Đã quá giờ hẹn uống thuốc: dc (5h) lúc 08:00 nhưng bạn chưa ghi nhận kết quả.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:02:31.0377161' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (218, N'DOCTOR', 0, N'SYSTEM', N'Cập nhật yêu cầu thay đổi', N'Bệnh nhân Do Manh Toan vừa cập nhật yêu cầu thay đổi phác đồ.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:14:59.0831006' AS DateTime2), 1, 7)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (219, N'DOCTOR', 0, N'SYSTEM', N'Cập nhật yêu cầu thay đổi', N'Bệnh nhân Do Manh Toan vừa cập nhật yêu cầu thay đổi phác đồ.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:17:59.9873049' AS DateTime2), 1, 7)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (220, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu thay đổi mới', N'Bệnh nhân Do Manh Toan vừa gửi một yêu cầu thay đổi phác đồ.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:19:14.3867838' AS DateTime2), 1, 7)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (221, N'DOCTOR', 0, N'SYSTEM', N'Yêu cầu thay đổi mới', N'Bệnh nhân Do Manh Toan vừa gửi một yêu cầu thay đổi phác đồ.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T08:22:05.7059264' AS DateTime2), 1, 7)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (222, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa sáng hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T09:00:00.2507512' AS DateTime2), 1, NULL)
GO
INSERT [dbo].[notifications] ([id], [recipient_type], [recipient_id], [notification_type], [title], [content], [channel], [related_entity_type], [related_entity_id], [status], [is_read], [sent_at], [read_at], [created_at], [patient_id], [doctor_id]) VALUES (223, N'PATIENT', 1, N'DIET_REMINDER', N'Nhắc nhở ghi nhận bữa ăn', N'Bạn chưa ghi nhận bữa sáng hôm nay.', N'IN_APP', NULL, NULL, N'SENT', 0, NULL, NULL, CAST(N'2026-07-20T09:00:00.2507512' AS DateTime2), 1, NULL)
GO
SET IDENTITY_INSERT [dbo].[notifications] OFF
GO
SET IDENTITY_INSERT [dbo].[nutrition_rules] ON 
GO
INSERT [dbo].[nutrition_rules] ([id], [patient_id], [doctor_id], [max_calories_per_day], [max_carbs_g], [max_salt_g], [min_fiber_g], [max_fat_g], [min_protein_g], [daily_water_ml], [additional_notes], [is_current], [effective_from], [created_at], [updated_at]) VALUES (1, 1, 7, 1800, CAST(220.00 AS Decimal(7, 2)), CAST(4.50 AS Decimal(6, 2)), CAST(22.00 AS Decimal(6, 2)), CAST(60.00 AS Decimal(7, 2)), CAST(80.00 AS Decimal(7, 2)), 2200, N'Chỉ tiêu dinh dưỡng tiêu chuẩn từ bác sĩ.', 1, CAST(N'2026-06-28' AS Date), CAST(N'2026-06-28T12:06:57.2537545' AS DateTime2), CAST(N'2026-06-28T12:06:57.2537545' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[nutrition_rules] OFF
GO
SET IDENTITY_INSERT [dbo].[otp_codes] ON 
GO
INSERT [dbo].[otp_codes] ([id], [email], [otp_code], [otp_type], [expires_at], [is_used], [created_at]) VALUES (1, N'domanht7@gmail.com', N'039552', N'REGISTRATION', CAST(N'2026-06-27T16:27:17.1064968' AS DateTime2), 1, CAST(N'2026-06-27T16:22:17.1064968' AS DateTime2))
GO
INSERT [dbo].[otp_codes] ([id], [email], [otp_code], [otp_type], [expires_at], [is_used], [created_at]) VALUES (2, N'domanht10@gmail.com', N'847163', N'REGISTRATION', CAST(N'2026-07-03T11:35:15.2912743' AS DateTime2), 1, CAST(N'2026-07-03T11:30:15.2912743' AS DateTime2))
GO
INSERT [dbo].[otp_codes] ([id], [email], [otp_code], [otp_type], [expires_at], [is_used], [created_at]) VALUES (3, N'domanht13@gmail.com', N'993738', N'REGISTRATION', CAST(N'2026-07-13T22:47:06.5999567' AS DateTime2), 1, CAST(N'2026-07-13T22:42:06.6010609' AS DateTime2))
GO
INSERT [dbo].[otp_codes] ([id], [email], [otp_code], [otp_type], [expires_at], [is_used], [created_at]) VALUES (4, N'domanht9@gmail.com', N'219635', N'REGISTRATION', CAST(N'2026-07-13T22:51:01.0898984' AS DateTime2), 0, CAST(N'2026-07-13T22:46:01.0898984' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[otp_codes] OFF
GO
SET IDENTITY_INSERT [dbo].[patient_medications] ON 
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (1, 1, N'tieu duong', N'1 viên', N'08:00', 1, CAST(N'2026-06-28T12:12:39.8692109' AS DateTime2), CAST(N'2026-06-28T12:12:39.8692109' AS DateTime2), NULL)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (2, 1, N'huyết áp', N'2 viên', N'08:00', 1, CAST(N'2026-06-28T12:18:36.8296984' AS DateTime2), CAST(N'2026-06-28T12:18:36.8296984' AS DateTime2), NULL)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (3, 1, N'huyết áp', N'2 viên', N'16:00', 0, CAST(N'2026-06-28T12:36:37.9899796' AS DateTime2), CAST(N'2026-06-28T16:08:44.5092321' AS DateTime2), NULL)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (4, 1, N'ha', N'5 gói', N'08:00', 1, CAST(N'2026-06-28T16:15:44.0081063' AS DateTime2), CAST(N'2026-06-28T16:15:44.0081063' AS DateTime2), NULL)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (5, 1, N'dc', N'5h', N'08:00', 1, CAST(N'2026-06-28T16:15:54.4443366' AS DateTime2), CAST(N'2026-06-28T16:15:54.4443366' AS DateTime2), NULL)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (6, 1, N'sa', N'3 b', N'16:46', 0, CAST(N'2026-06-28T16:45:17.2780081' AS DateTime2), CAST(N'2026-06-28T16:50:06.1885387' AS DateTime2), NULL)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (7, 1, N'sa', N'3 b', N'16:52', 0, CAST(N'2026-06-28T16:50:22.6568121' AS DateTime2), CAST(N'2026-06-28T16:59:34.8539643' AS DateTime2), NULL)
GO
INSERT [dbo].[patient_medications] ([id], [patient_id], [medicine_name], [dosage], [scheduled_time], [is_active], [created_at], [updated_at], [treatment_plan_id]) VALUES (8, 1, N'test1', N'2b', N'17:01', 1, CAST(N'2026-06-28T17:00:03.3158306' AS DateTime2), CAST(N'2026-06-28T17:00:03.3158306' AS DateTime2), NULL)
GO
SET IDENTITY_INSERT [dbo].[patient_medications] OFF
GO
SET IDENTITY_INSERT [dbo].[patients] ON 
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (1, 8, 1, 7, 3, N'PAT-0001', N'Do Manh Toan', CAST(N'2005-01-26' AS Date), N'MALE', N'0390220292', N'Chương Mỹ', N'sadsad ', N'0390220292', N'TREATING', N'ONLINE', 1, CAST(N'2026-06-27T19:29:00.4799490' AS DateTime2), CAST(N'2026-06-27T16:22:16.9404692' AS DateTime2), CAST(N'2026-06-27T20:21:03.8254416' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (2, 13, 1, 7, 1, N'PAT-0002', N'Đỗ Mạnh Toàn', CAST(N'2005-10-26' AS Date), N'FEMALE', N'0389765105', N'Ha Noi', N'Đỗ Mạnh To', N'0389765104', N'TREATING', N'ONLINE', 1, CAST(N'2026-07-13T22:38:53.2577091' AS DateTime2), CAST(N'2026-07-03T11:30:15.2482935' AS DateTime2), CAST(N'2026-07-13T22:38:53.2577091' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (3, 14, 1, 7, 2, NULL, N'Lê Trung Th?ng', CAST(N'2005-10-26' AS Date), N'MALE', N'0389765100', N'Ha Noi', N'Đ? M?nh To', N'0389765122', N'TREATING', N'ONLINE', 1, CAST(N'2026-07-13T22:44:31.9226994' AS DateTime2), CAST(N'2026-07-13T22:42:55.7795766' AS DateTime2), CAST(N'2026-07-13T22:44:31.9226994' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (4, 4, 1, 5, NULL, NULL, N'benh.nhan.1', NULL, NULL, N'0987848079', NULL, NULL, NULL, N'TREATING', N'ONLINE', 1, NULL, CAST(N'2026-07-20T07:55:03.8633333' AS DateTime2), CAST(N'2026-07-20T07:55:03.8633333' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (5, 5, 1, 5, NULL, NULL, N'benh.nhan.2', NULL, NULL, N'0987861194', NULL, NULL, NULL, N'TREATING', N'ONLINE', 1, NULL, CAST(N'2026-07-20T07:55:03.8666667' AS DateTime2), CAST(N'2026-07-20T07:55:03.8666667' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (6, 6, 1, 5, NULL, NULL, N'benh.nhan.3', NULL, NULL, N'0987581143', NULL, NULL, NULL, N'TREATING', N'ONLINE', 1, NULL, CAST(N'2026-07-20T07:55:03.8666667' AS DateTime2), CAST(N'2026-07-20T07:55:03.8666667' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (7, 7, 1, 5, NULL, NULL, N'benh.nhan.4', NULL, NULL, N'0987849477', NULL, NULL, NULL, N'TREATING', N'ONLINE', 1, NULL, CAST(N'2026-07-20T07:55:03.8666667' AS DateTime2), CAST(N'2026-07-20T07:55:03.8666667' AS DateTime2))
GO
INSERT [dbo].[patients] ([id], [account_id], [hospital_id], [doctor_id], [disease_profile_id], [patient_code], [full_name], [date_of_birth], [gender], [phone], [address], [emergency_contact_name], [emergency_contact_phone], [status], [registration_source], [is_active], [onboarded_at], [created_at], [updated_at]) VALUES (8, 15, 1, 5, NULL, NULL, N'domanht9', NULL, NULL, N'0987975335', NULL, NULL, NULL, N'TREATING', N'ONLINE', 1, NULL, CAST(N'2026-07-20T07:55:03.8700000' AS DateTime2), CAST(N'2026-07-20T07:55:03.8700000' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[patients] OFF
GO
SET IDENTITY_INSERT [dbo].[water_logs] ON 
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml], [logged_at]) VALUES (1, 1, CAST(N'2026-06-28' AS Date), 250, NULL)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml], [logged_at]) VALUES (2, 1, CAST(N'2026-07-20' AS Date), 2500, NULL)
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml], [logged_at]) VALUES (5, 1, CAST(N'2026-07-20' AS Date), 250, CAST(N'2026-07-20T08:41:15.493' AS DateTime))
GO
INSERT [dbo].[water_logs] ([id], [patient_id], [log_date], [amount_ml], [logged_at]) VALUES (6, 1, CAST(N'2026-07-20' AS Date), 500, CAST(N'2026-07-20T08:41:17.173' AS DateTime))
GO
SET IDENTITY_INSERT [dbo].[water_logs] OFF
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_accounts_email]    Script Date: 7/21/2026 6:25:43 PM ******/
ALTER TABLE [dbo].[accounts] ADD  CONSTRAINT [UQ_accounts_email] UNIQUE NONCLUSTERED 
(
	[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_alerts_doctor_unresolved]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_alerts_doctor_unresolved] ON [dbo].[alerts]
(
	[doctor_id] ASC,
	[alert_level] DESC,
	[triggered_at] DESC
)
INCLUDE([patient_id],[alert_color],[metric_type],[alert_message]) 
WHERE ([is_resolved]=(0))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_alerts_hospital_overview]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_alerts_hospital_overview] ON [dbo].[alerts]
(
	[is_resolved] ASC,
	[alert_level] ASC,
	[triggered_at] DESC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_appointments_doctor_pending]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_appointments_doctor_pending] ON [dbo].[appointments]
(
	[doctor_id] ASC,
	[status] ASC,
	[appointment_time] ASC
)
INCLUDE([patient_id],[appointment_type],[created_by]) 
WHERE ([status]='PENDING')
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_appointments_patient_upcoming]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_appointments_patient_upcoming] ON [dbo].[appointments]
(
	[patient_id] ASC,
	[appointment_time] ASC
)
INCLUDE([doctor_id],[status],[appointment_type],[location]) 
WHERE ([status] IN ('PENDING', 'ACCEPTED'))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_audit_trails_actor_action]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_audit_trails_actor_action] ON [dbo].[audit_trails]
(
	[actor_type] ASC,
	[actor_id] ASC,
	[created_at] DESC
)
INCLUDE([action],[target_table],[target_record_id]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_audit_trails_target]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_audit_trails_target] ON [dbo].[audit_trails]
(
	[target_table] ASC,
	[target_record_id] ASC,
	[created_at] DESC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_daily_health_logs_patient_date]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_daily_health_logs_patient_date] ON [dbo].[daily_health_logs]
(
	[patient_id] ASC,
	[log_date] DESC
)
INCLUDE([log_type],[systolic_bp],[diastolic_bp],[heart_rate],[glucose_level],[alert_level],[is_alert_processed]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_daily_health_logs_unprocessed_alerts]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_daily_health_logs_unprocessed_alerts] ON [dbo].[daily_health_logs]
(
	[patient_id] ASC,
	[alert_level] ASC,
	[is_alert_processed] ASC
)
INCLUDE([log_date],[log_time],[systolic_bp],[diastolic_bp],[glucose_level]) 
WHERE ([is_alert_processed]=(0) AND [alert_level] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_diet_logs_patient_date]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_diet_logs_patient_date] ON [dbo].[diet_logs]
(
	[patient_id] ASC,
	[log_date] DESC
)
INCLUDE([food_id],[meal_type],[quantity_g]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_disease_profiles_profile_code]    Script Date: 7/21/2026 6:25:43 PM ******/
ALTER TABLE [dbo].[disease_profiles] ADD  CONSTRAINT [UQ_disease_profiles_profile_code] UNIQUE NONCLUSTERED 
(
	[profile_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_doctors_account_id]    Script Date: 7/21/2026 6:25:43 PM ******/
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [UQ_doctors_account_id] UNIQUE NONCLUSTERED 
(
	[account_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_doctors_doctor_code]    Script Date: 7/21/2026 6:25:43 PM ******/
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [UQ_doctors_doctor_code] UNIQUE NONCLUSTERED 
(
	[doctor_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_doctors_phone]    Script Date: 7/21/2026 6:25:43 PM ******/
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [UQ_doctors_phone] UNIQUE NONCLUSTERED 
(
	[phone] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_doctors_available_capacity]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_doctors_available_capacity] ON [dbo].[doctors]
(
	[hospital_id] ASC,
	[current_patient_count] ASC,
	[capacity_limit] ASC
)
INCLUDE([doctor_code],[full_name],[specialty]) 
WHERE ([is_active]=(1))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_doctors_workload_monitoring]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_doctors_workload_monitoring] ON [dbo].[doctors]
(
	[is_active] ASC,
	[hospital_id] ASC
)
INCLUDE([doctor_code],[full_name],[current_patient_count],[capacity_limit]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_exercise_logs_patient_date]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_exercise_logs_patient_date] ON [dbo].[exercise_logs]
(
	[patient_id] ASC,
	[log_date] DESC
)
INCLUDE([exercise_type],[duration_minutes],[steps_count]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_foods_dictionary_code]    Script Date: 7/21/2026 6:25:43 PM ******/
ALTER TABLE [dbo].[foods_dictionary] ADD  CONSTRAINT [UQ_foods_dictionary_code] UNIQUE NONCLUSTERED 
(
	[food_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_foods_dictionary_search]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_foods_dictionary_search] ON [dbo].[foods_dictionary]
(
	[food_name] ASC
)
INCLUDE([food_code],[energy_kcal],[protein_g],[lipid_g],[glucid_g]) 
WHERE ([is_active]=(1))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_hospitals_account_id]    Script Date: 7/21/2026 6:25:43 PM ******/
ALTER TABLE [dbo].[hospitals] ADD  CONSTRAINT [UQ_hospitals_account_id] UNIQUE NONCLUSTERED 
(
	[account_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_hospitals_hospital_code]    Script Date: 7/21/2026 6:25:43 PM ******/
ALTER TABLE [dbo].[hospitals] ADD  CONSTRAINT [UQ_hospitals_hospital_code] UNIQUE NONCLUSTERED 
(
	[hospital_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_medication_logs_med_date]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_medication_logs_med_date] ON [dbo].[medication_logs]
(
	[patient_medication_id] ASC,
	[log_date] ASC
)
INCLUDE([is_taken],[taken_at]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_notifications_recipient_unread]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_notifications_recipient_unread] ON [dbo].[notifications]
(
	[recipient_type] ASC,
	[recipient_id] ASC,
	[is_read] ASC,
	[created_at] DESC
)
INCLUDE([notification_type],[title]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_otp_codes_validation]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_otp_codes_validation] ON [dbo].[otp_codes]
(
	[email] ASC,
	[otp_type] ASC,
	[is_used] ASC,
	[expires_at] ASC
)
INCLUDE([otp_code]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_patient_medications_patient_active]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_patient_medications_patient_active] ON [dbo].[patient_medications]
(
	[patient_id] ASC,
	[is_active] ASC
)
INCLUDE([medicine_name],[dosage],[scheduled_time]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_patients_account_id]    Script Date: 7/21/2026 6:25:43 PM ******/
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [UQ_patients_account_id] UNIQUE NONCLUSTERED 
(
	[account_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_patients_phone]    Script Date: 7/21/2026 6:25:43 PM ******/
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [UQ_patients_phone] UNIQUE NONCLUSTERED 
(
	[phone] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_patients_account_login]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_patients_account_login] ON [dbo].[patients]
(
	[account_id] ASC
)
INCLUDE([id],[status],[is_active]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_patients_doctor_status]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_patients_doctor_status] ON [dbo].[patients]
(
	[doctor_id] ASC,
	[status] ASC
)
INCLUDE([patient_code],[full_name],[phone],[disease_profile_id],[is_active]) 
WHERE ([is_active]=(1))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_patients_hospital_overview]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_patients_hospital_overview] ON [dbo].[patients]
(
	[hospital_id] ASC,
	[status] ASC,
	[is_active] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_system_logs_time_level]    Script Date: 7/21/2026 6:25:43 PM ******/
CREATE NONCLUSTERED INDEX [IX_system_logs_time_level] ON [dbo].[system_logs]
(
	[created_at] DESC,
	[log_level] ASC
)
INCLUDE([module_name],[event_code],[related_patient_id]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[accounts] ADD  CONSTRAINT [DF_accounts_verified]  DEFAULT ((0)) FOR [is_email_verified]
GO
ALTER TABLE [dbo].[accounts] ADD  CONSTRAINT [DF_accounts_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[accounts] ADD  CONSTRAINT [DF_accounts_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[accounts] ADD  CONSTRAINT [DF_accounts_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[alert_thresholds] ADD  CONSTRAINT [DF_alert_thresholds_scope]  DEFAULT ('HOSPITAL') FOR [scope]
GO
ALTER TABLE [dbo].[alert_thresholds] ADD  CONSTRAINT [DF_alert_thresholds_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[alert_thresholds] ADD  CONSTRAINT [DF_alert_thresholds_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[alerts] ADD  CONSTRAINT [DF_alerts_is_resolved]  DEFAULT ((0)) FOR [is_resolved]
GO
ALTER TABLE [dbo].[alerts] ADD  CONSTRAINT [DF_alerts_triggered_at]  DEFAULT (sysdatetime()) FOR [triggered_at]
GO
ALTER TABLE [dbo].[alerts] ADD  CONSTRAINT [DF_alerts_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[appointments] ADD  CONSTRAINT [DF_appointments_status]  DEFAULT ('PENDING') FOR [status]
GO
ALTER TABLE [dbo].[appointments] ADD  CONSTRAINT [DF_appointments_type]  DEFAULT ('CHECKUP') FOR [appointment_type]
GO
ALTER TABLE [dbo].[appointments] ADD  CONSTRAINT [DF_appointments_created_by]  DEFAULT ('PATIENT') FOR [created_by]
GO
ALTER TABLE [dbo].[appointments] ADD  CONSTRAINT [DF_appointments_reminder_2days]  DEFAULT ((0)) FOR [reminder_sent_2days]
GO
ALTER TABLE [dbo].[appointments] ADD  CONSTRAINT [DF_appointments_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[appointments] ADD  CONSTRAINT [DF_appointments_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[audit_trails] ADD  CONSTRAINT [DF_audit_trails_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[clinical_records] ADD  CONSTRAINT [DF_clinical_records_is_initial]  DEFAULT ((0)) FOR [is_initial_exam]
GO
ALTER TABLE [dbo].[clinical_records] ADD  CONSTRAINT [DF_clinical_records_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[clinical_records] ADD  CONSTRAINT [DF_clinical_records_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[change_requests] ADD  CONSTRAINT [DF_change_requests_status]  DEFAULT ('PENDING') FOR [status]
GO
ALTER TABLE [dbo].[change_requests] ADD  CONSTRAINT [DF_change_requests_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[change_requests] ADD  CONSTRAINT [DF_change_requests_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[daily_health_logs] ADD  CONSTRAINT [DF_daily_health_logs_input_method]  DEFAULT ('MANUAL') FOR [input_method]
GO
ALTER TABLE [dbo].[daily_health_logs] ADD  CONSTRAINT [DF_daily_health_logs_ocr_valid]  DEFAULT ((0)) FOR [is_ocr_validated]
GO
ALTER TABLE [dbo].[daily_health_logs] ADD  CONSTRAINT [DF_daily_health_logs_is_alert_processed]  DEFAULT ((0)) FOR [is_alert_processed]
GO
ALTER TABLE [dbo].[daily_health_logs] ADD  CONSTRAINT [DF_daily_health_logs_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[diet_logs] ADD  CONSTRAINT [DF_diet_logs_logged_at]  DEFAULT (sysdatetime()) FOR [logged_at]
GO
ALTER TABLE [dbo].[disease_profiles] ADD  CONSTRAINT [DF_disease_profiles_requires_bp]  DEFAULT ((0)) FOR [requires_bp_input]
GO
ALTER TABLE [dbo].[disease_profiles] ADD  CONSTRAINT [DF_disease_profiles_requires_glucose]  DEFAULT ((0)) FOR [requires_glucose_input]
GO
ALTER TABLE [dbo].[disease_profiles] ADD  CONSTRAINT [DF_disease_profiles_is_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [DF_doctors_capacity_limit]  DEFAULT ((50)) FOR [capacity_limit]
GO
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [DF_doctors_current_patient_count]  DEFAULT ((0)) FOR [current_patient_count]
GO
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [DF_doctors_is_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [DF_doctors_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[doctors] ADD  CONSTRAINT [DF_doctors_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[emergency_guides] ADD  CONSTRAINT [DF_emergency_guides_is_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[emergency_guides] ADD  CONSTRAINT [DF_emergency_guides_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[emergency_guides] ADD  CONSTRAINT [DF_emergency_guides_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[emergency_protocols] ADD  CONSTRAINT [DF_emergency_protocols_is_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[emergency_protocols] ADD  CONSTRAINT [DF_emergency_protocols_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[emergency_protocols] ADD  CONSTRAINT [DF_emergency_protocols_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[exercise_logs] ADD  CONSTRAINT [DF_exercise_logs_logged_at]  DEFAULT (sysdatetime()) FOR [logged_at]
GO
ALTER TABLE [dbo].[foods_dictionary] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[foods_dictionary] ADD  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[hospitals] ADD  CONSTRAINT [DF_hospitals_is_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[hospitals] ADD  CONSTRAINT [DF_hospitals_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[hospitals] ADD  CONSTRAINT [DF_hospitals_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[medication_logs] ADD  DEFAULT ((0)) FOR [is_taken]
GO
ALTER TABLE [dbo].[notifications] ADD  CONSTRAINT [DF_notifications_channel]  DEFAULT ('IN_APP') FOR [channel]
GO
ALTER TABLE [dbo].[notifications] ADD  CONSTRAINT [DF_notifications_status]  DEFAULT ('PENDING') FOR [status]
GO
ALTER TABLE [dbo].[notifications] ADD  CONSTRAINT [DF_notifications_is_read]  DEFAULT ((0)) FOR [is_read]
GO
ALTER TABLE [dbo].[notifications] ADD  CONSTRAINT [DF_notifications_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[nutrition_rules] ADD  CONSTRAINT [DF_nutrition_rules_water]  DEFAULT ((2000)) FOR [daily_water_ml]
GO
ALTER TABLE [dbo].[nutrition_rules] ADD  CONSTRAINT [DF_nutrition_rules_is_current]  DEFAULT ((1)) FOR [is_current]
GO
ALTER TABLE [dbo].[nutrition_rules] ADD  CONSTRAINT [DF_nutrition_rules_effective_from]  DEFAULT (CONVERT([date],sysdatetime())) FOR [effective_from]
GO
ALTER TABLE [dbo].[nutrition_rules] ADD  CONSTRAINT [DF_nutrition_rules_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[nutrition_rules] ADD  CONSTRAINT [DF_nutrition_rules_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[otp_codes] ADD  CONSTRAINT [DF_otp_codes_is_used]  DEFAULT ((0)) FOR [is_used]
GO
ALTER TABLE [dbo].[otp_codes] ADD  CONSTRAINT [DF_otp_codes_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[patient_medications] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[patient_medications] ADD  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[patient_medications] ADD  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [DF_patients_status]  DEFAULT ('NEW') FOR [status]
GO
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [DF_patients_registration_source]  DEFAULT ('ONLINE') FOR [registration_source]
GO
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [DF_patients_is_active]  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [DF_patients_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[patients] ADD  CONSTRAINT [DF_patients_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[system_logs] ADD  CONSTRAINT [DF_system_logs_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[treatment_plans] ADD  CONSTRAINT [DF_treatment_plans_is_current]  DEFAULT ((1)) FOR [is_current]
GO
ALTER TABLE [dbo].[treatment_plans] ADD  CONSTRAINT [DF_treatment_plans_effective_from]  DEFAULT (CONVERT([date],sysdatetime())) FOR [effective_from]
GO
ALTER TABLE [dbo].[treatment_plans] ADD  CONSTRAINT [DF_treatment_plans_created_at]  DEFAULT (sysdatetime()) FOR [created_at]
GO
ALTER TABLE [dbo].[treatment_plans] ADD  CONSTRAINT [DF_treatment_plans_updated_at]  DEFAULT (sysdatetime()) FOR [updated_at]
GO
ALTER TABLE [dbo].[water_logs] ADD  DEFAULT ((0)) FOR [amount_ml]
GO
ALTER TABLE [dbo].[alert_thresholds]  WITH CHECK ADD  CONSTRAINT [FK_alert_thresholds_doctor_id] FOREIGN KEY([created_by_doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[alert_thresholds] CHECK CONSTRAINT [FK_alert_thresholds_doctor_id]
GO
ALTER TABLE [dbo].[alert_thresholds]  WITH CHECK ADD  CONSTRAINT [FK_alert_thresholds_hospital_id] FOREIGN KEY([hospital_id])
REFERENCES [dbo].[hospitals] ([id])
GO
ALTER TABLE [dbo].[alert_thresholds] CHECK CONSTRAINT [FK_alert_thresholds_hospital_id]
GO
ALTER TABLE [dbo].[alert_thresholds]  WITH CHECK ADD  CONSTRAINT [FK_alert_thresholds_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[alert_thresholds] CHECK CONSTRAINT [FK_alert_thresholds_patient_id]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [FK_alerts_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [FK_alerts_doctor_id]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [FK_alerts_health_log_id] FOREIGN KEY([health_log_id])
REFERENCES [dbo].[daily_health_logs] ([id])
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [FK_alerts_health_log_id]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [FK_alerts_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [FK_alerts_patient_id]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [FK_alerts_resolved_by_doctor_id] FOREIGN KEY([resolved_by_doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [FK_alerts_resolved_by_doctor_id]
GO
ALTER TABLE [dbo].[appointments]  WITH CHECK ADD  CONSTRAINT [FK_appointments_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[appointments] CHECK CONSTRAINT [FK_appointments_doctor_id]
GO
ALTER TABLE [dbo].[appointments]  WITH CHECK ADD  CONSTRAINT [FK_appointments_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[appointments] CHECK CONSTRAINT [FK_appointments_patient_id]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [FK_clinical_records_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [FK_clinical_records_doctor_id]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [FK_clinical_records_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [FK_clinical_records_patient_id]
GO
ALTER TABLE [dbo].[change_requests]  WITH CHECK ADD  CONSTRAINT [FK_change_requests_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[change_requests] CHECK CONSTRAINT [FK_change_requests_doctor_id]
GO
ALTER TABLE [dbo].[change_requests]  WITH CHECK ADD  CONSTRAINT [FK_change_requests_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[change_requests] CHECK CONSTRAINT [FK_change_requests_patient_id]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [FK_daily_health_logs_alert_processed_by] FOREIGN KEY([alert_processed_by])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [FK_daily_health_logs_alert_processed_by]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [FK_daily_health_logs_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [FK_daily_health_logs_patient_id]
GO
ALTER TABLE [dbo].[diet_logs]  WITH CHECK ADD  CONSTRAINT [FK_diet_logs_food_id] FOREIGN KEY([food_id])
REFERENCES [dbo].[foods_dictionary] ([id])
GO
ALTER TABLE [dbo].[diet_logs] CHECK CONSTRAINT [FK_diet_logs_food_id]
GO
ALTER TABLE [dbo].[diet_logs]  WITH CHECK ADD  CONSTRAINT [FK_diet_logs_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[diet_logs] CHECK CONSTRAINT [FK_diet_logs_patient_id]
GO
ALTER TABLE [dbo].[doctors]  WITH CHECK ADD  CONSTRAINT [FK_doctors_account_id] FOREIGN KEY([account_id])
REFERENCES [dbo].[accounts] ([id])
GO
ALTER TABLE [dbo].[doctors] CHECK CONSTRAINT [FK_doctors_account_id]
GO
ALTER TABLE [dbo].[doctors]  WITH CHECK ADD  CONSTRAINT [FK_doctors_hospital_id] FOREIGN KEY([hospital_id])
REFERENCES [dbo].[hospitals] ([id])
GO
ALTER TABLE [dbo].[doctors] CHECK CONSTRAINT [FK_doctors_hospital_id]
GO
ALTER TABLE [dbo].[emergency_guides]  WITH CHECK ADD  CONSTRAINT [FK_emergency_guides_hospital_id] FOREIGN KEY([hospital_id])
REFERENCES [dbo].[hospitals] ([id])
GO
ALTER TABLE [dbo].[emergency_guides] CHECK CONSTRAINT [FK_emergency_guides_hospital_id]
GO
ALTER TABLE [dbo].[emergency_protocols]  WITH CHECK ADD  CONSTRAINT [FK_emergency_protocols_hospital_id] FOREIGN KEY([hospital_id])
REFERENCES [dbo].[hospitals] ([id])
GO
ALTER TABLE [dbo].[emergency_protocols] CHECK CONSTRAINT [FK_emergency_protocols_hospital_id]
GO
ALTER TABLE [dbo].[exercise_guidelines]  WITH CHECK ADD  CONSTRAINT [FKa4o0qrc7tcjdf70jhtbmna0jc] FOREIGN KEY([hospital_id])
REFERENCES [dbo].[hospitals] ([id])
GO
ALTER TABLE [dbo].[exercise_guidelines] CHECK CONSTRAINT [FKa4o0qrc7tcjdf70jhtbmna0jc]
GO
ALTER TABLE [dbo].[exercise_guidelines]  WITH CHECK ADD  CONSTRAINT [FKto1gmi614o4pp0inxpya033qh] FOREIGN KEY([disease_profile_id])
REFERENCES [dbo].[disease_profiles] ([id])
GO
ALTER TABLE [dbo].[exercise_guidelines] CHECK CONSTRAINT [FKto1gmi614o4pp0inxpya033qh]
GO
ALTER TABLE [dbo].[exercise_logs]  WITH CHECK ADD  CONSTRAINT [FK_exercise_logs_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[exercise_logs] CHECK CONSTRAINT [FK_exercise_logs_patient_id]
GO
ALTER TABLE [dbo].[hospitals]  WITH CHECK ADD  CONSTRAINT [FK_hospitals_account_id] FOREIGN KEY([account_id])
REFERENCES [dbo].[accounts] ([id])
GO
ALTER TABLE [dbo].[hospitals] CHECK CONSTRAINT [FK_hospitals_account_id]
GO
ALTER TABLE [dbo].[medication_logs]  WITH CHECK ADD  CONSTRAINT [FK_medication_logs_patient_medications] FOREIGN KEY([patient_medication_id])
REFERENCES [dbo].[patient_medications] ([id])
GO
ALTER TABLE [dbo].[medication_logs] CHECK CONSTRAINT [FK_medication_logs_patient_medications]
GO

ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [FKmiuhx5o7tg0vb1nkjhw7y9udi] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [FKmiuhx5o7tg0vb1nkjhw7y9udi]
GO
ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [FKsxbhag07yf88eve8uuor8tll1] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [FKsxbhag07yf88eve8uuor8tll1]
GO
ALTER TABLE [dbo].[nutrition_rules]  WITH CHECK ADD  CONSTRAINT [FK_nutrition_rules_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[nutrition_rules] CHECK CONSTRAINT [FK_nutrition_rules_doctor_id]
GO
ALTER TABLE [dbo].[nutrition_rules]  WITH CHECK ADD  CONSTRAINT [FK_nutrition_rules_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[nutrition_rules] CHECK CONSTRAINT [FK_nutrition_rules_patient_id]
GO
ALTER TABLE [dbo].[patient_medications]  WITH CHECK ADD  CONSTRAINT [FK_patient_medications_patients] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[patient_medications] CHECK CONSTRAINT [FK_patient_medications_patients]
GO
ALTER TABLE [dbo].[patient_medications]  WITH CHECK ADD  CONSTRAINT [FKchygdc1ped3rvv0mxw8wurpua] FOREIGN KEY([treatment_plan_id])
REFERENCES [dbo].[treatment_plans] ([id])
GO
ALTER TABLE [dbo].[patient_medications] CHECK CONSTRAINT [FKchygdc1ped3rvv0mxw8wurpua]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [FK_patients_account_id] FOREIGN KEY([account_id])
REFERENCES [dbo].[accounts] ([id])
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [FK_patients_account_id]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [FK_patients_disease_profile_id] FOREIGN KEY([disease_profile_id])
REFERENCES [dbo].[disease_profiles] ([id])
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [FK_patients_disease_profile_id]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [FK_patients_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [FK_patients_doctor_id]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [FK_patients_hospital_id] FOREIGN KEY([hospital_id])
REFERENCES [dbo].[hospitals] ([id])
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [FK_patients_hospital_id]
GO
ALTER TABLE [dbo].[treatment_plans]  WITH CHECK ADD  CONSTRAINT [FK_treatment_plans_doctor_id] FOREIGN KEY([doctor_id])
REFERENCES [dbo].[doctors] ([id])
GO
ALTER TABLE [dbo].[treatment_plans] CHECK CONSTRAINT [FK_treatment_plans_doctor_id]
GO
ALTER TABLE [dbo].[treatment_plans]  WITH CHECK ADD  CONSTRAINT [FK_treatment_plans_nutrition_rule_id] FOREIGN KEY([nutrition_rule_id])
REFERENCES [dbo].[nutrition_rules] ([id])
GO
ALTER TABLE [dbo].[treatment_plans] CHECK CONSTRAINT [FK_treatment_plans_nutrition_rule_id]
GO
ALTER TABLE [dbo].[treatment_plans]  WITH CHECK ADD  CONSTRAINT [FK_treatment_plans_patient_id] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[treatment_plans] CHECK CONSTRAINT [FK_treatment_plans_patient_id]
GO
ALTER TABLE [dbo].[water_logs]  WITH CHECK ADD  CONSTRAINT [FK_water_logs_patient] FOREIGN KEY([patient_id])
REFERENCES [dbo].[patients] ([id])
GO
ALTER TABLE [dbo].[water_logs] CHECK CONSTRAINT [FK_water_logs_patient]
GO
ALTER TABLE [dbo].[accounts]  WITH CHECK ADD  CONSTRAINT [CHK_accounts_role] CHECK  (([role]='HOSPITAL_ADMIN' OR [role]='DOCTOR' OR [role]='PATIENT'))
GO
ALTER TABLE [dbo].[accounts] CHECK CONSTRAINT [CHK_accounts_role]
GO
ALTER TABLE [dbo].[alert_thresholds]  WITH CHECK ADD  CONSTRAINT [CHK_alert_thresholds_metric_type] CHECK  (([metric_type]='COMBINED' OR [metric_type]='HEART_RATE' OR [metric_type]='BLOOD_PRESSURE' OR [metric_type]='GLUCOSE'))
GO
ALTER TABLE [dbo].[alert_thresholds] CHECK CONSTRAINT [CHK_alert_thresholds_metric_type]
GO
ALTER TABLE [dbo].[alert_thresholds]  WITH CHECK ADD  CONSTRAINT [CHK_alert_thresholds_scope] CHECK  (([scope]='PATIENT' OR [scope]='HOSPITAL'))
GO
ALTER TABLE [dbo].[alert_thresholds] CHECK CONSTRAINT [CHK_alert_thresholds_scope]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [CHK_alerts_alert_color] CHECK  (([alert_color]='RED' OR [alert_color]='ORANGE' OR [alert_color]='YELLOW' OR [alert_color]='GREEN'))
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [CHK_alerts_alert_color]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [CHK_alerts_alert_level] CHECK  (([alert_level]=(4) OR [alert_level]=(3) OR [alert_level]=(2) OR [alert_level]=(1)))
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [CHK_alerts_alert_level]
GO
ALTER TABLE [dbo].[alerts]  WITH CHECK ADD  CONSTRAINT [CHK_alerts_metric_type] CHECK  (([metric_type]='BOTH' OR [metric_type]='HEART_RATE' OR [metric_type]='BLOOD_PRESSURE' OR [metric_type]='GLUCOSE'))
GO
ALTER TABLE [dbo].[alerts] CHECK CONSTRAINT [CHK_alerts_metric_type]
GO
ALTER TABLE [dbo].[appointments]  WITH CHECK ADD  CONSTRAINT [CHK_appointments_created_by] CHECK  (([created_by]='DOCTOR' OR [created_by]='PATIENT'))
GO
ALTER TABLE [dbo].[appointments] CHECK CONSTRAINT [CHK_appointments_created_by]
GO
ALTER TABLE [dbo].[appointments]  WITH CHECK ADD  CONSTRAINT [CHK_appointments_status] CHECK  (([status]='CANCELLED' OR [status]='COMPLETED' OR [status]='REJECTED' OR [status]='ACCEPTED' OR [status]='PENDING'))
GO
ALTER TABLE [dbo].[appointments] CHECK CONSTRAINT [CHK_appointments_status]
GO
ALTER TABLE [dbo].[appointments]  WITH CHECK ADD  CONSTRAINT [CHK_appointments_type] CHECK  (([appointment_type]='FOLLOWUP' OR [appointment_type]='INITIAL' OR [appointment_type]='CHECKUP'))
GO
ALTER TABLE [dbo].[appointments] CHECK CONSTRAINT [CHK_appointments_type]
GO
ALTER TABLE [dbo].[audit_trails]  WITH CHECK ADD  CONSTRAINT [CHK_audit_trails_actor_type] CHECK  (([actor_type]='HOSPITAL_ADMIN' OR [actor_type]='PATIENT' OR [actor_type]='DOCTOR'))
GO
ALTER TABLE [dbo].[audit_trails] CHECK CONSTRAINT [CHK_audit_trails_actor_type]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [CHK_clinical_records_diastolic_bp] CHECK  (([diastolic_bp] IS NULL OR [diastolic_bp]>=(30) AND [diastolic_bp]<=(200)))
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [CHK_clinical_records_diastolic_bp]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [CHK_clinical_records_fasting_glucose] CHECK  (([fasting_glucose] IS NULL OR [fasting_glucose]>=(1.0) AND [fasting_glucose]<=(33.3)))
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [CHK_clinical_records_fasting_glucose]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [CHK_clinical_records_heart_rate] CHECK  (([heart_rate] IS NULL OR [heart_rate]>=(20) AND [heart_rate]<=(300)))
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [CHK_clinical_records_heart_rate]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [CHK_clinical_records_systolic_bp] CHECK  (([systolic_bp] IS NULL OR [systolic_bp]>=(50) AND [systolic_bp]<=(300)))
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [CHK_clinical_records_systolic_bp]
GO
ALTER TABLE [dbo].[clinical_records]  WITH CHECK ADD  CONSTRAINT [CHK_clinical_records_weight] CHECK  (([weight_kg] IS NULL OR [weight_kg]>(0) AND [weight_kg]<(500)))
GO
ALTER TABLE [dbo].[clinical_records] CHECK CONSTRAINT [CHK_clinical_records_weight]
GO
ALTER TABLE [dbo].[change_requests]  WITH CHECK ADD  CONSTRAINT [CHK_change_requests_request_type] CHECK  (([request_type]='APPOINTMENT' OR [request_type]='DIET' OR [request_type]='SCHEDULE' OR [request_type]='TREATMENT_PLAN'))
GO
ALTER TABLE [dbo].[change_requests] CHECK CONSTRAINT [CHK_change_requests_request_type]
GO
ALTER TABLE [dbo].[change_requests]  WITH CHECK ADD  CONSTRAINT [CHK_change_requests_status] CHECK  (([status]='IN_REVIEW' OR [status]='REJECTED' OR [status]='APPROVED' OR [status]='PENDING'))
GO
ALTER TABLE [dbo].[change_requests] CHECK CONSTRAINT [CHK_change_requests_status]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_alert_level] CHECK  (([alert_level]='RED' OR [alert_level]='ORANGE' OR [alert_level]='YELLOW' OR [alert_level]='GREEN' OR [alert_level] IS NULL))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_alert_level]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_diastolic_bp] CHECK  (([diastolic_bp] IS NULL OR [diastolic_bp]>=(30) AND [diastolic_bp]<=(200)))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_diastolic_bp]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_glucose] CHECK  (([glucose_level] IS NULL OR [glucose_level]>=(1.0) AND [glucose_level]<=(33.3)))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_glucose]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_heart_rate] CHECK  (([heart_rate] IS NULL OR [heart_rate]>=(20) AND [heart_rate]<=(300)))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_heart_rate]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_input_method] CHECK  (([input_method]='OCR' OR [input_method]='MANUAL'))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_input_method]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_log_type] CHECK  (([log_type]='RANDOM' OR [log_type]='EVENING' OR [log_type]='MORNING'))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_log_type]
GO
ALTER TABLE [dbo].[daily_health_logs]  WITH CHECK ADD  CONSTRAINT [CHK_daily_health_logs_systolic_bp] CHECK  (([systolic_bp] IS NULL OR [systolic_bp]>=(50) AND [systolic_bp]<=(300)))
GO
ALTER TABLE [dbo].[daily_health_logs] CHECK CONSTRAINT [CHK_daily_health_logs_systolic_bp]
GO
ALTER TABLE [dbo].[diet_logs]  WITH CHECK ADD  CONSTRAINT [CHK_diet_logs_meal_type] CHECK  (([meal_type]='SNACK' OR [meal_type]='DINNER' OR [meal_type]='LUNCH' OR [meal_type]='BREAKFAST'))
GO
ALTER TABLE [dbo].[diet_logs] CHECK CONSTRAINT [CHK_diet_logs_meal_type]
GO
ALTER TABLE [dbo].[diet_logs]  WITH CHECK ADD  CONSTRAINT [CHK_diet_logs_quantity] CHECK  (([quantity_g]>(0)))
GO
ALTER TABLE [dbo].[diet_logs] CHECK CONSTRAINT [CHK_diet_logs_quantity]
GO
ALTER TABLE [dbo].[disease_profiles]  WITH CHECK ADD  CONSTRAINT [CHK_disease_profiles_profile_code] CHECK  (([profile_code]='BOTH' OR [profile_code]='DIABETES' OR [profile_code]='HYPERTENSION'))
GO
ALTER TABLE [dbo].[disease_profiles] CHECK CONSTRAINT [CHK_disease_profiles_profile_code]
GO
ALTER TABLE [dbo].[doctors]  WITH CHECK ADD  CONSTRAINT [CHK_doctors_capacity_limit] CHECK  (([capacity_limit]>=(1) AND [capacity_limit]<=(500)))
GO
ALTER TABLE [dbo].[doctors] CHECK CONSTRAINT [CHK_doctors_capacity_limit]
GO
ALTER TABLE [dbo].[doctors]  WITH CHECK ADD  CONSTRAINT [CHK_doctors_count_not_exceed_limit] CHECK  (([current_patient_count]<=[capacity_limit]))
GO
ALTER TABLE [dbo].[doctors] CHECK CONSTRAINT [CHK_doctors_count_not_exceed_limit]
GO
ALTER TABLE [dbo].[doctors]  WITH CHECK ADD  CONSTRAINT [CHK_doctors_current_patient_count] CHECK  (([current_patient_count]>=(0)))
GO
ALTER TABLE [dbo].[doctors] CHECK CONSTRAINT [CHK_doctors_current_patient_count]
GO
ALTER TABLE [dbo].[doctors]  WITH CHECK ADD  CONSTRAINT [CHK_doctors_gender] CHECK  (([gender]='OTHER' OR [gender]='FEMALE' OR [gender]='MALE' OR [gender] IS NULL))
GO
ALTER TABLE [dbo].[doctors] CHECK CONSTRAINT [CHK_doctors_gender]
GO
ALTER TABLE [dbo].[emergency_guides]  WITH CHECK ADD  CONSTRAINT [CHK_emergency_guides_alert_level] CHECK  (([alert_level]='RED' OR [alert_level]='ORANGE' OR [alert_level]='YELLOW' OR [alert_level]='GREEN'))
GO
ALTER TABLE [dbo].[emergency_guides] CHECK CONSTRAINT [CHK_emergency_guides_alert_level]
GO
ALTER TABLE [dbo].[emergency_guides]  WITH CHECK ADD  CONSTRAINT [CHK_emergency_guides_metric_type] CHECK  (([metric_type]='BOTH' OR [metric_type]='BLOOD_PRESSURE' OR [metric_type]='GLUCOSE'))
GO
ALTER TABLE [dbo].[emergency_guides] CHECK CONSTRAINT [CHK_emergency_guides_metric_type]
GO
ALTER TABLE [dbo].[emergency_protocols]  WITH CHECK ADD  CONSTRAINT [CHK_emergency_protocols_condition_type] CHECK  (([condition_type]='HYPERGLYCEMIA' OR [condition_type]='HYPOGLYCEMIA' OR [condition_type]='HYPERTENSIVE_CRISIS'))
GO
ALTER TABLE [dbo].[emergency_protocols] CHECK CONSTRAINT [CHK_emergency_protocols_condition_type]
GO
ALTER TABLE [dbo].[exercise_logs]  WITH CHECK ADD  CONSTRAINT [CHK_exercise_logs_duration] CHECK  (([duration_minutes] IS NULL OR [duration_minutes]>(0)))
GO
ALTER TABLE [dbo].[exercise_logs] CHECK CONSTRAINT [CHK_exercise_logs_duration]
GO
ALTER TABLE [dbo].[exercise_logs]  WITH CHECK ADD  CONSTRAINT [CHK_exercise_logs_steps] CHECK  (([steps_count] IS NULL OR [steps_count]>=(0)))
GO
ALTER TABLE [dbo].[exercise_logs] CHECK CONSTRAINT [CHK_exercise_logs_steps]
GO
ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [CHK_notifications_channel] CHECK  (([channel]='SMS' OR [channel]='EMAIL' OR [channel]='PUSH' OR [channel]='IN_APP'))
GO
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [CHK_notifications_channel]
GO
ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [CHK_notifications_recipient_type] CHECK  (([recipient_type]='HOSPITAL_ADMIN' OR [recipient_type]='DOCTOR' OR [recipient_type]='PATIENT'))
GO
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [CHK_notifications_recipient_type]
GO
ALTER TABLE [dbo].[notifications]  WITH CHECK ADD  CONSTRAINT [CHK_notifications_status] CHECK  (([status]='READ' OR [status]='FAILED' OR [status]='SENT' OR [status]='PENDING'))
GO
ALTER TABLE [dbo].[notifications] CHECK CONSTRAINT [CHK_notifications_status]
GO
ALTER TABLE [dbo].[nutrition_rules]  WITH CHECK ADD  CONSTRAINT [CHK_nutrition_rules_calories] CHECK  (([max_calories_per_day]>=(500) AND [max_calories_per_day]<=(5000)))
GO
ALTER TABLE [dbo].[nutrition_rules] CHECK CONSTRAINT [CHK_nutrition_rules_calories]
GO
ALTER TABLE [dbo].[nutrition_rules]  WITH CHECK ADD  CONSTRAINT [CHK_nutrition_rules_salt] CHECK  (([max_salt_g]>=(0) AND [max_salt_g]<=(20)))
GO
ALTER TABLE [dbo].[nutrition_rules] CHECK CONSTRAINT [CHK_nutrition_rules_salt]
GO
ALTER TABLE [dbo].[otp_codes]  WITH CHECK ADD  CONSTRAINT [CHK_otp_codes_type] CHECK  (([otp_type]='PASSWORD_RESET' OR [otp_type]='REGISTRATION'))
GO
ALTER TABLE [dbo].[otp_codes] CHECK CONSTRAINT [CHK_otp_codes_type]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [CHK_patients_gender] CHECK  (([gender]='OTHER' OR [gender]='FEMALE' OR [gender]='MALE' OR [gender] IS NULL))
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [CHK_patients_gender]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [CHK_patients_registration_source] CHECK  (([registration_source]='CLINIC' OR [registration_source]='ONLINE'))
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [CHK_patients_registration_source]
GO
ALTER TABLE [dbo].[patients]  WITH CHECK ADD  CONSTRAINT [CHK_patients_status] CHECK  (([status]='INACTIVE' OR [status]='TREATING' OR [status]='NEW'))
GO
ALTER TABLE [dbo].[patients] CHECK CONSTRAINT [CHK_patients_status]
GO
ALTER TABLE [dbo].[system_logs]  WITH CHECK ADD  CONSTRAINT [CHK_system_logs_log_level] CHECK  (([log_level]='CRITICAL' OR [log_level]='ERROR' OR [log_level]='WARNING' OR [log_level]='INFO'))
GO
ALTER TABLE [dbo].[system_logs] CHECK CONSTRAINT [CHK_system_logs_log_level]
GO
/****** Object:  StoredProcedure [dbo].[sp_assign_patient_to_doctor]    Script Date: 7/21/2026 6:25:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================================
-- BƯỚC 4: STORED PROCEDURES
-- ============================================================
 
-- ------------------------------------------------------------
-- SP 1: sp_assign_patient_to_doctor  (không đổi logic)
-- ------------------------------------------------------------
CREATE   PROCEDURE [dbo].[sp_assign_patient_to_doctor]
    @patient_id         INT,
    @doctor_id          INT,
    @actor_id           INT,
    @actor_type         VARCHAR(20),
    @result_message     NVARCHAR(500)   OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
 
    DECLARE @doctor_is_active       BIT;
    DECLARE @doctor_capacity_limit  INT;
    DECLARE @doctor_current_count   INT;
    DECLARE @doctor_full_name       NVARCHAR(255);
    DECLARE @patient_exists         INT;
    DECLARE @patient_current_doctor INT;
 
    BEGIN TRANSACTION;
    BEGIN TRY
 
        SELECT
            @doctor_is_active       = is_active,
            @doctor_capacity_limit  = capacity_limit,
            @doctor_current_count   = current_patient_count,
            @doctor_full_name       = full_name
        FROM doctors WITH (UPDLOCK)
        WHERE id = @doctor_id;
 
        IF @doctor_is_active IS NULL
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Không tìm thấy bác sĩ.';
            RETURN -1;
        END
 
        IF @doctor_is_active = 0
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Tài khoản bác sĩ đã bị vô hiệu hóa.';
            RETURN -2;
        END
 
        IF @doctor_current_count >= @doctor_capacity_limit
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Bác sĩ đã đạt ngưỡng tải tối đa.';
            RETURN -3;
        END
 
        SELECT
            @patient_exists         = id,
            @patient_current_doctor = doctor_id
        FROM patients WITH (UPDLOCK)
        WHERE id = @patient_id;
 
        IF @patient_exists IS NULL
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Không tìm thấy bệnh nhân.';
            RETURN -4;
        END
 
        IF @patient_current_doctor IS NOT NULL AND @patient_current_doctor <> @doctor_id
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Bệnh nhân đã có bác sĩ phụ trách. Cần chuyển giao trước.';
            RETURN -5;
        END
 
        IF @patient_current_doctor = @doctor_id
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Thông báo: Bệnh nhân đã được gán cho bác sĩ này rồi.';
            RETURN 0;
        END
 
        UPDATE patients
        SET doctor_id = @doctor_id, status = 'TREATING', onboarded_at = SYSDATETIME(), updated_at = SYSDATETIME()
        WHERE id = @patient_id;
 
        UPDATE doctors
        SET current_patient_count = current_patient_count + 1, updated_at = SYSDATETIME()
        WHERE id = @doctor_id;
 
        INSERT INTO audit_trails (actor_type, actor_id, action, target_table, target_record_id, new_value)
        VALUES (
            @actor_type, @actor_id, 'ASSIGN_PATIENT_TO_DOCTOR', 'patients', @patient_id,
            N'{"doctor_id":' + CAST(@doctor_id AS VARCHAR) + ', "status":"TREATING"}'
        );
 
        COMMIT TRANSACTION;
        SET @result_message = N'Thành công: Đã gán bệnh nhân cho bác sĩ [' + @doctor_full_name + '].';
        RETURN 0;
 
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        SET @result_message = N'Lỗi hệ thống khi gán bệnh nhân: ' + ERROR_MESSAGE();
        RETURN -99;
    END CATCH;
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_available_doctors]    Script Date: 7/21/2026 6:25:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
 
-- ------------------------------------------------------------
-- SP 3: sp_get_available_doctors (không đổi logic)
-- ------------------------------------------------------------
CREATE   PROCEDURE [dbo].[sp_get_available_doctors]
    @hospital_id    INT             = NULL,
    @specialty      NVARCHAR(255)   = NULL
AS
BEGIN
    SET NOCOUNT ON;
 
    SELECT
        d.id                    AS doctor_id,
        d.doctor_code,
        d.full_name             AS doctor_name,
        d.specialty,
        d.phone,
        d.capacity_limit,
        d.current_patient_count,
        d.capacity_limit - d.current_patient_count  AS available_slots,
        CAST(
            (CAST(d.current_patient_count AS FLOAT) / CAST(d.capacity_limit AS FLOAT)) * 100
        AS DECIMAL(5,2))                            AS workload_percentage,
        h.full_name             AS hospital_name
    FROM doctors d
    INNER JOIN hospitals h ON d.hospital_id = h.id
    WHERE
        d.is_active = 1
        AND d.current_patient_count < d.capacity_limit
        AND h.is_active = 1
        AND (@hospital_id IS NULL OR d.hospital_id = @hospital_id)
        AND (@specialty IS NULL OR d.specialty = @specialty)
    ORDER BY
        workload_percentage ASC,
        d.full_name ASC;
 
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_doctor_dashboard]    Script Date: 7/21/2026 6:25:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
 
-- ------------------------------------------------------------
-- SP 5: sp_get_doctor_dashboard
-- [SỬA] Bỏ mức CRITICAL trong phân loại màu dashboard.
-- ------------------------------------------------------------
CREATE   PROCEDURE [dbo].[sp_get_doctor_dashboard]
    @doctor_id  INT,
    @target_date DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
 
    IF @target_date IS NULL SET @target_date = CAST(SYSDATETIME() AS DATE);
 
    SELECT
        p.id                    AS patient_id,
        p.patient_code,
        p.full_name             AS patient_name,
        p.phone,
        dp.profile_code         AS disease_profile,
        latest_log.systolic_bp          AS today_systolic_bp,
        latest_log.diastolic_bp         AS today_diastolic_bp,
        latest_log.glucose_level        AS today_glucose,
        latest_log.alert_level          AS today_alert_level,
        latest_log.log_time             AS last_log_time,
        dashboard_color = ISNULL(latest_log.alert_level, 'GREY'),   -- GREEN/YELLOW/ORANGE/RED hoặc GREY (chưa nhập liệu)
        unresolved_alerts.alert_count   AS unresolved_alert_count,
        morning_log.id                  AS has_morning_log,
        evening_log.id                  AS has_evening_log
    FROM patients p
    LEFT JOIN disease_profiles dp ON p.disease_profile_id = dp.id
    LEFT JOIN (
        SELECT DISTINCT
            dhl.patient_id,
            FIRST_VALUE(dhl.systolic_bp)    OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS systolic_bp,
            FIRST_VALUE(dhl.diastolic_bp)   OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS diastolic_bp,
            FIRST_VALUE(dhl.glucose_level)  OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS glucose_level,
            FIRST_VALUE(dhl.alert_level)    OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS alert_level,
            FIRST_VALUE(dhl.log_time)       OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS log_time
        FROM daily_health_logs dhl
        WHERE dhl.log_date = @target_date
    ) latest_log ON p.id = latest_log.patient_id
    LEFT JOIN (
        SELECT patient_id, COUNT(*) AS alert_count
        FROM alerts
        WHERE is_resolved = 0
        GROUP BY patient_id
    ) unresolved_alerts ON p.id = unresolved_alerts.patient_id
    LEFT JOIN daily_health_logs morning_log
        ON p.id = morning_log.patient_id
        AND morning_log.log_date = @target_date
        AND morning_log.log_type = 'MORNING'
    LEFT JOIN daily_health_logs evening_log
        ON p.id = evening_log.patient_id
        AND evening_log.log_date = @target_date
        AND evening_log.log_type = 'EVENING'
    WHERE
        p.doctor_id     = @doctor_id
        AND p.status    = 'TREATING'
        AND p.is_active = 1
    ORDER BY
        -- Ưu tiên hiển thị: Đỏ -> Cam -> Vàng -> Xám (chưa nhập) -> Xanh
        CASE
            WHEN latest_log.alert_level = 'RED'    THEN 1
            WHEN latest_log.alert_level = 'ORANGE' THEN 2
            WHEN latest_log.alert_level = 'YELLOW' THEN 3
            WHEN latest_log.alert_level IS NULL    THEN 4
            ELSE 5
        END,
        unresolved_alerts.alert_count DESC;
 
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_get_hospital_overview]    Script Date: 7/21/2026 6:25:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
 
-- ------------------------------------------------------------
-- SP 6: sp_get_hospital_overview
-- [SỬA] Đếm "cảnh báo nghiêm trọng" theo alert_color = 'RED' (không còn CRITICAL).
-- ------------------------------------------------------------
CREATE   PROCEDURE [dbo].[sp_get_hospital_overview]
    @hospital_id    INT
AS
BEGIN
    SET NOCOUNT ON;
 
    SELECT
        h.full_name             AS hospital_name,
        (SELECT COUNT(*) FROM patients p
         WHERE p.hospital_id = @hospital_id AND p.status = 'TREATING' AND p.is_active = 1)
                                AS total_treating_patients,
        (SELECT COUNT(*) FROM patients p
         WHERE p.hospital_id = @hospital_id AND p.status = 'NEW' AND p.is_active = 1)
                                AS total_new_patients,
        -- Số cảnh báo mức RED (cấp cứu) chưa xử lý toàn viện
        (SELECT COUNT(*) FROM alerts a
         INNER JOIN patients p ON a.patient_id = p.id
         WHERE p.hospital_id = @hospital_id AND a.is_resolved = 0 AND a.alert_color = 'RED')
                                AS red_unresolved_alerts,
        (SELECT COUNT(*) FROM doctors d
         WHERE d.hospital_id = @hospital_id AND d.is_active = 1)
                                AS active_doctors_count
    FROM hospitals h
    WHERE h.id = @hospital_id;
 
    SELECT
        d.id                    AS doctor_id,
        d.doctor_code,
        d.full_name             AS doctor_name,
        d.specialty,
        d.current_patient_count,
        d.capacity_limit,
        CAST(
            (CAST(d.current_patient_count AS FLOAT) / CAST(d.capacity_limit AS FLOAT)) * 100
        AS DECIMAL(5,2))        AS workload_percentage,
        workload_status = CASE
            WHEN (CAST(d.current_patient_count AS FLOAT) / d.capacity_limit) >= 1.0   THEN 'FULL'
            WHEN (CAST(d.current_patient_count AS FLOAT) / d.capacity_limit) >= 0.9   THEN 'CRITICAL'
            WHEN (CAST(d.current_patient_count AS FLOAT) / d.capacity_limit) >= 0.7   THEN 'HIGH'
            WHEN (CAST(d.current_patient_count AS FLOAT) / d.capacity_limit) >= 0.5   THEN 'MEDIUM'
            ELSE 'LOW'
        END,
        d.is_active
    FROM doctors d
    WHERE d.hospital_id = @hospital_id
    ORDER BY workload_percentage DESC;
 
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_record_daily_health_log]    Script Date: 7/21/2026 6:25:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[sp_record_daily_health_log]
    @patient_id         INT,
    @log_type           VARCHAR(10),
    @input_method       VARCHAR(10),
    @systolic_bp        INT             = NULL,
    @diastolic_bp       INT             = NULL,
    @heart_rate         INT             = NULL,
    @glucose_level       DECIMAL(4,2)    = NULL,     -- mmol/L
    @image_url          VARCHAR(500)    = NULL,
    @patient_notes       NVARCHAR(500)   = NULL,
    @new_log_id          INT             OUTPUT,
    @alert_level_result   VARCHAR(10)     OUTPUT,
    @result_message     NVARCHAR(500)   OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
 
    DECLARE @bp_alert               VARCHAR(10) = 'GREEN';
    DECLARE @glucose_alert          VARCHAR(10) = 'GREEN';
    DECLARE @calculated_alert_level VARCHAR(10) = 'GREEN';
    DECLARE @hospital_id            INT;
    DECLARE @is_ocr_validated       BIT;
 
    SET @is_ocr_validated = CASE WHEN @input_method = 'OCR' THEN 0 ELSE 1 END;
 
    SELECT @hospital_id = hospital_id
    FROM patients
    WHERE id = @patient_id AND is_active = 1;
 
    IF @hospital_id IS NULL
    BEGIN
        SET @result_message = N'Lá»—i: Bá»‡nh nhÃ¢n khÃ´ng tá»“n táº¡i hoáº·c Ä‘Ã£ bá»‹ vÃ´ hiá»‡u hÃ³a.';
        RETURN -1;
    END
 
    -- ---- TÃ­nh má»©c cáº£nh bÃ¡o huyáº¿t Ã¡p ----
    IF @systolic_bp IS NOT NULL OR @diastolic_bp IS NOT NULL
    BEGIN
        IF (@systolic_bp >= 180 OR @diastolic_bp >= 110)
            SET @bp_alert = 'RED';
        ELSE IF (@systolic_bp BETWEEN 140 AND 179) OR (@diastolic_bp BETWEEN 90 AND 109)
            SET @bp_alert = 'ORANGE';
        ELSE IF (@systolic_bp BETWEEN 130 AND 139) OR (@diastolic_bp BETWEEN 85 AND 89)
            SET @bp_alert = 'YELLOW';
        ELSE IF (ISNULL(@systolic_bp, 0) < 120) AND (ISNULL(@diastolic_bp, 0) < 80)
            SET @bp_alert = 'GREEN';
        ELSE
            -- VÃ¹ng trung gian chÆ°a quy Ä‘á»‹nh rÃµ (VD: systolic 120-129 / diastolic 80-84)
            -- táº¡m xáº¿p má»©c YELLOW Ä‘á»ƒ bá»‡nh nhÃ¢n lÆ°u Ã½ theo dÃµi thÃªm.
            SET @bp_alert = 'YELLOW';
    END
 
    -- ---- TÃ­nh má»©c cáº£nh bÃ¡o Ä‘Æ°á»ng huyáº¿t (mmol/L) ----
    IF @glucose_level IS NOT NULL
    BEGIN
        IF @glucose_level < 4.4
            SET @glucose_alert = 'RED';        -- Háº¡ Ä‘Æ°á»ng huyáº¿t Ä‘á»™t ngá»™t
        ELSE IF @glucose_level > 16
            SET @glucose_alert = 'RED';        -- TÄƒng Ä‘Æ°á»ng huyáº¿t kháº©n cáº¥p
        ELSE IF @glucose_level > 10
            SET @glucose_alert = 'ORANGE';     -- Cao
        ELSE
            SET @glucose_alert = 'GREEN';      -- BÃ¬nh thÆ°á»ng (4.4 - 10)
    END
 
    -- ---- Láº¥y má»©c cao nháº¥t giá»¯a hai chá»‰ sá»‘ ----
    SET @calculated_alert_level = CASE
        WHEN 'RED'    IN (@bp_alert, @glucose_alert) THEN 'RED'
        WHEN 'ORANGE' IN (@bp_alert, @glucose_alert) THEN 'ORANGE'
        WHEN 'YELLOW' IN (@bp_alert, @glucose_alert) THEN 'YELLOW'
        ELSE 'GREEN'
    END;
 
    INSERT INTO daily_health_logs (
        patient_id, log_date, log_time, log_type,
        systolic_bp, diastolic_bp, heart_rate, glucose_level,
        input_method, image_url, is_ocr_validated,
        alert_level, patient_notes
    )
    VALUES (
        @patient_id,
        CAST(SYSDATETIME() AS DATE),
        SYSDATETIME(),
        @log_type,
        @systolic_bp, @diastolic_bp, @heart_rate, @glucose_level,
        @input_method, @image_url,
        @is_ocr_validated,
        @calculated_alert_level,
        @patient_notes
    );
 
    SET @new_log_id = SCOPE_IDENTITY();
 
    -- Theo nghiá»‡p vá»¥ 2.3: chá»‰ táº¡o báº£n ghi alerts (vÃ  bÃ¡o bÃ¡c sÄ©) khi má»©c ORANGE hoáº·c RED
    IF @calculated_alert_level IN ('ORANGE', 'RED')
    BEGIN
        DECLARE @doctor_id       INT;
        DECLARE @alert_level_num INT;
 
        SELECT @doctor_id = doctor_id FROM patients WHERE id = @patient_id;
 
        SET @alert_level_num = CASE @calculated_alert_level
            WHEN 'ORANGE' THEN 3
            WHEN 'RED'    THEN 4
        END;
 
        IF @doctor_id IS NOT NULL
        BEGIN
            INSERT INTO alerts (
                patient_id, doctor_id, health_log_id, alert_level, alert_color,
                metric_type, metric_value, threshold_violated, alert_message
            )
            VALUES (
                @patient_id,
                @doctor_id,
                @new_log_id,
                @alert_level_num,
                @calculated_alert_level,
                CASE
                    WHEN @systolic_bp IS NOT NULL AND @glucose_level IS NOT NULL THEN 'BOTH'
                    WHEN @systolic_bp IS NOT NULL THEN 'BLOOD_PRESSURE'
                    ELSE 'GLUCOSE'
                END,
                N'HA: ' + ISNULL(CAST(@systolic_bp AS VARCHAR), 'N/A') + '/' + ISNULL(CAST(@diastolic_bp AS VARCHAR), 'N/A')
                + N' mmHg | Glucose: ' + ISNULL(CAST(@glucose_level AS VARCHAR), 'N/A') + N' mmol/L',
                N'VÆ°á»£t ngÆ°á»¡ng cáº£nh bÃ¡o má»©c ' + @calculated_alert_level,
                N'Bá»‡nh nhÃ¢n ID ' + CAST(@patient_id AS VARCHAR)
                + N' cÃ³ chá»‰ sá»‘ vÆ°á»£t ngÆ°á»¡ng: Má»©c ' + @calculated_alert_level
                + N'. Cáº§n kiá»ƒm tra ngay.'
            );
 
            -- Äá»“ng thá»i ghi vÃ o notifications Ä‘á»ƒ gá»­i thÃ´ng bÃ¡o tá»›i bÃ¡c sÄ©
            INSERT INTO notifications (recipient_type, recipient_id, notification_type, title, content, related_entity_type, related_entity_id)
            VALUES (
                'DOCTOR', @doctor_id, 'ALERT',
                N'Cáº£nh bÃ¡o sá»©c khá»e má»©c ' + @calculated_alert_level,
                N'Bá»‡nh nhÃ¢n ID ' + CAST(@patient_id AS VARCHAR) + N' cÃ³ chá»‰ sá»‘ vÆ°á»£t ngÆ°á»¡ng má»©c ' + @calculated_alert_level + N'.',
                'DAILY_HEALTH_LOG', @new_log_id
            );
        END
    END
 
    SET @alert_level_result = @calculated_alert_level;
    SET @result_message = N'ThÃ nh cÃ´ng: ÄÃ£ ghi nháº­n nháº­t kÃ½ sá»©c khá»e. Má»©c cáº£nh bÃ¡o: ' + @calculated_alert_level;
    RETURN 0;
 
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_unassign_patient_from_doctor]    Script Date: 7/21/2026 6:25:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
 
-- ------------------------------------------------------------
-- SP 2: sp_unassign_patient_from_doctor (không đổi logic)
-- ------------------------------------------------------------
CREATE   PROCEDURE [dbo].[sp_unassign_patient_from_doctor]
    @patient_id         INT,
    @unassigned_by      INT,
    @actor_type         VARCHAR(20),
    @reason             NVARCHAR(500),
    @result_message     NVARCHAR(500)   OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
 
    DECLARE @current_doctor_id      INT;
    DECLARE @doctor_current_count   INT;
 
    SELECT @current_doctor_id = doctor_id
    FROM patients
    WHERE id = @patient_id;
 
    IF @current_doctor_id IS NULL
    BEGIN
        SET @result_message = N'Thông báo: Bệnh nhân chưa được gán cho bác sĩ nào.';
        RETURN 0;
    END
 
    SELECT @doctor_current_count = current_patient_count
    FROM doctors
    WHERE id = @current_doctor_id;
 
    BEGIN TRANSACTION;
    BEGIN TRY
 
        UPDATE patients
        SET
            doctor_id   = NULL,
            status      = 'INACTIVE',
            updated_at  = SYSDATETIME()
        WHERE id = @patient_id;
 
        UPDATE doctors
        SET
            current_patient_count   = CASE WHEN current_patient_count > 0
                                           THEN current_patient_count - 1
                                           ELSE 0 END,
            updated_at              = SYSDATETIME()
        WHERE id = @current_doctor_id;
 
        INSERT INTO audit_trails (actor_type, actor_id, action, target_table, target_record_id, old_value, notes)
        VALUES (
            @actor_type,
            @unassigned_by,
            'UNASSIGN_PATIENT_FROM_DOCTOR',
            'patients',
            @patient_id,
            N'{"doctor_id":' + CAST(@current_doctor_id AS VARCHAR) + '}',
            @reason
        );
 
        COMMIT TRANSACTION;
 
        SET @result_message = N'Thành công: Đã gỡ bệnh nhân (ID = ' + CAST(@patient_id AS VARCHAR)
                             + N') khỏi bác sĩ (ID = ' + CAST(@current_doctor_id AS VARCHAR)
                             + N'). Dữ liệu y tế được giữ nguyên.';
        RETURN 0;
 
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @result_message = N'Lỗi hệ thống khi gỡ gán bệnh nhân: ' + ERROR_MESSAGE();
        RETURN -99;
    END CATCH;
 
END;
GO
USE [master]
GO
ALTER DATABASE [RemotePatientMonitoringVer2] SET  READ_WRITE 
GO
